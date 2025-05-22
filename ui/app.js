const express = require('express');
const axios = require('axios');
const bodyParser = require('body-parser');
const redis = require('redis');
const ejsLayouts = require('express-ejs-layouts');

const app = express();

const API_BASE = process.env.API_BASE || 'http://localhost:8087/api/v1';
const REDIS_URL = process.env.REDIS_URL || 'redis://localhost:6379';
const TTL = 30;

const client = redis.createClient({ url: REDIS_URL });

client.on('error', (err) => {
    console.error('Redis Client Error:', err);
});

app.set('view engine', 'ejs');
app.use(ejsLayouts);
app.use(bodyParser.urlencoded({ extended: true }));
app.use(express.static('public'));

function cacheMiddleware(pageKey) {
    return async (req, res, next) => {
        try {
            const html = await client.get(pageKey);
            if (html) {
                console.log(`[CACHE HIT] ${req.method} ${req.originalUrl} => ${pageKey}`);
                return res.send(html);
            }
            console.log(`[CACHE MISS] ${req.method} ${req.originalUrl} => ${pageKey}`);

            res.sendResponse = res.send;
            res.send = (body) => {
                client.setEx(pageKey, TTL, body).catch(err =>
                    console.error(`Redis setEx error for ${pageKey}:`, err.message)
                );
                res.sendResponse(body);
            };

            next();
        } catch (err) {
            console.error(`Cache error for ${pageKey}:`, err.message);
            next();
        }
    };
}

app.get('/', cacheMiddleware('page:index'), (req, res) => {
    res.render('index', { title: 'Главная' });
});

app.get('/users', cacheMiddleware('page:users'), async (req, res) => {
    try {
        const users = await axios.get(`${API_BASE}/users`).then(r => r.data);

        const visits = {};
        for (const user of users) {
            const key = `user:visits:${user.id}`;
            const count = await client.get(key);
            visits[user.id] = Number.isFinite(parseInt(count)) ? parseInt(count) : 0;
        }

        res.render('users', { title: 'Пользователи', users, visits });
    } catch (err) {
        console.error('Error loading users:', err.message);
        res.render('users', { title: 'Пользователи', users: [], visits: {} });
    }
});


app.get('/users/new', (req, res) => {
    res.render('user_new', { title: 'Создать пользователя' });
});

app.post('/users', async (req, res) => {
    try {
        await axios.post(`${API_BASE}/users`, req.body);
        await client.del('page:users');
        console.log('Cache invalidated: page:users');
    } catch (err) {
        console.error('Error creating user:', err.message);
    }
    res.redirect('/users');
});

app.get('/events/new', (req, res) => {
    res.render('event_new', { title: 'Создать событие' });
});

app.post('/events', async (req, res) => {
    try {
        await axios.post(`${API_BASE}/events`, req.body);
        await client.del('page:events');
        console.log('Cache invalidated: page:events');
    } catch (err) {
        console.error('Error creating event:', err.message);
    }
    res.redirect('/events');
});

app.post('/users/:id/visit', async (req, res) => {
    const userId = req.params.id;
    try {
        await axios.post(`${API_BASE}/users/${userId}/visit`);
        await client.del('page:users'); // Invalidate full page cache
    } catch (err) {
        console.error(`Error visiting user ${userId}:`, err.message);
    }
    res.redirect('/users');
});

app.post('/users/:id/visit-massive', async (req, res) => {
    const userId = req.params.id;
    const visitRequests = [];

    for (let i = 0; i < 1000; i++) {
        visitRequests.push(
            axios.post(`${API_BASE}/users/${userId}/visit`).catch(err =>
                console.error(`Failed visit #${i + 1}:`, err.message)
            )
        );
    }

    await Promise.all(visitRequests);
    await client.del('page:users'); // очистка кеша

    res.redirect('/users');
});

app.post('/users/:id/visit-incr', async (req, res) => {
    const userId = req.params.id;
    const visitRequests = [];

    for (let i = 0; i < 1000; i++) {
        visitRequests.push(
            axios.post(`${API_BASE}/users/${userId}/visit-incr`).catch(err =>
                console.error(`Failed visit-watch #${i + 1}:`, err.message)
            )
        );
    }

    await Promise.all(visitRequests);
    await client.del('page:users');

    res.redirect('/users');

});

app.post('/users/:id/visit-watch', async (req, res) => {
    const userId = req.params.id;
    const visitRequests = [];

    for (let i = 0; i < 1000; i++) {
        visitRequests.push(
            axios.post(`${API_BASE}/users/${userId}/visit-watch`).catch(err =>
                console.error(`Failed visit-watch #${i + 1}:`, err.message)
            )
        );
    }

    await Promise.all(visitRequests);
    await client.del('page:users');

    res.redirect('/users');

});

app.post('/users/:id/visit-lock', async (req, res) => {
    const userId = req.params.id;
    const visitRequests = [];

    for (let i = 0; i < 1000; i++) {
        visitRequests.push(
            axios.post(`${API_BASE}/users/${userId}/visit-lock`).catch(err =>
                console.error(`Failed visit-lock #${i + 1}:`, err.message)
            )
        );
    }

    await Promise.all(visitRequests);
    await client.del('page:users');

    res.redirect('/users');

});

(async () => {
    try {
        await client.connect();
        app.listen(3000, () => {
            console.log(`UI server running at http://localhost:3000`);
            console.log(`Using API: ${API_BASE}`);
            console.log(`Redis: ${REDIS_URL}`);
        });
    } catch (err) {
        console.error('Failed to start server:', err);
        process.exit(1);
    }
})();
