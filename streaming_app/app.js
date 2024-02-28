require('dotenv').config();
const mysql = require('mysql2');
const express = require('express');
const axios = require('axios');
const session = require('express-session');
const app = express();
const port = process.env.PORT || 3000;
const path = require('path');
const fs = require('fs');

app.set('views', path.join(__dirname, 'views'));
app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.set('view engine', 'ejs');
// app.use('/videos', express.static(path.join(__dirname, 'videos')));

app.use(session({
    secret: process.env.SESSION_SECRET,
    resave: false,
    saveUninitialized: true,
    cookie: { secure: false }
}));

const connection = mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    port: process.env.DB_PORT
});



connection.connect((err) => {
    if (err) {
        console.log('Error connecting to Db');
        return;
    }
    console.log('Connection established');
});


app.get('/register',checkNotAuth, (req, res) => {
    res.render('register');
});

app.post('/register', async (req, res) => {
    try {
        const response = await axios.post(process.env.AUTH_SERVICE_URL + '/register', req.body);
        res.redirect('/login');
    } catch (error) {
        res.send('Error registering user');
    }
});

app.get('/login',checkNotAuth, (req, res) => {
    res.render('login');
});

app.post('/login', async (req, res) => {
    try {
        const response = await axios.post(process.env.AUTH_SERVICE_URL + '/login', req.body);
        req.session.token = response.data.token;
        res.redirect('/');
    } catch (error) {
        res.send('Error logging in');
    }
});

app.get('/logout', (req, res) => {
    req.session.destroy(err => {
        if(err) {
            return res.redirect('/');
        }
        res.clearCookie('sid');
        res.redirect('/login');
    });
});


function checkAuth(req, res, next) {
    if (req.session.token) {
        axios.get(process.env.AUTH_SERVICE_URL + '/validate', {
            headers: {
                Authorization: 'Bearer ' + req.session.token
            }
        }).then(response => {
            next();
        }).catch(error => {
            res.redirect('/login');
        });
    } else {
        res.redirect('/login');
    }
}

function checkNotAuth(req, res, next) {
    if (req.session.token) {
        res.redirect('/');
    } else {
        next();
    }
}





app.get('/logout', (req, res) => {
    req.session.token = null;
    res.redirect('/login');
});



// return the index.html file along with the list of videos
let videos;
app.get('/', checkAuth, (req, res) => {
    connection.query(
        'SELECT * FROM video',
        function(err, results, fields) {
            if (err) {
                res.status(500).send('Error retrieving videos from database');
                return;
            }
            videos = results;

            res.render('index', { videos: results });
        }
    );
});

// Stream the videos. The videos variable consists of the list of videos (videos.url) eg. /videos/video1.mp4
app.get('/videos/:url', checkAuth, (req, res) => {
    let video = videos.find(video => video.url === req.params.url);
    if (!video) {
        res.status(404).send('Video not found');
        return;
    }
    
    const path = 'videos/' + video.url;
    const stat = fs.statSync(path);
    const fileSize = stat.size;
    const range = req.headers.range;
    if (range) {
        const parts = range.replace(/bytes=/, "").split("-");
        const start = parseInt(parts[0], 10);
        const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;
        const chunksize = (end - start) + 1;
        const file = fs.createReadStream(path, { start, end });
        const head = {
            'Content-Range': `bytes ${start}-${end}/${fileSize}`,
            'Accept-Ranges': 'bytes',
            'Content-Length': chunksize,
            'Content-Type': 'video/mp4',
        };
        res.writeHead(206, head);
        file.pipe(res);
    } else {
        const head = {
            'Content-Length': fileSize,
            'Content-Type': 'video/mp4',
        };
        res.writeHead(200, head);
        fs.createReadStream(path).pipe(res);
    }
});


app.listen(port, () => {
    console.log(`Streaming app listening at http://localhost:${port}`);
});