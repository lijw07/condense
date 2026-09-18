(function () {
    'use strict';

    function send(method, url, body) {
        var options = { method: method, headers: {} };
        if (body !== undefined) {
            options.headers['Content-Type'] = 'application/json';
            options.body = JSON.stringify(body);
        }
        return fetch(url, options).then(function (response) {
            if (!response.ok) {
                return response.text().then(function (text) {
                    throw new Error(text || response.statusText);
                });
            }
            return response.status === 204 ? null : response.json().catch(function () { return null; });
        });
    }

    function showError(message) {
        var banner = document.querySelector('[data-error]');
        if (!banner) {
            window.alert(message);
            return;
        }
        banner.textContent = message;
        banner.classList.remove('hidden');
    }

    function debounce(fn, wait) {
        var timer;
        return function () {
            var args = arguments;
            window.clearTimeout(timer);
            timer = window.setTimeout(function () { fn.apply(null, args); }, wait);
        };
    }

    function bindSignOut() {
        var form = document.querySelector('[data-sign-out]');
        if (!form) return;
        form.addEventListener('submit', function (event) {
            event.preventDefault();
            send('POST', '/auth/sign-out').then(function () {
                window.location.href = '/';
            }).catch(function (error) { showError(error.message); });
        });
    }

    function bindStartForm() {
        var form = document.querySelector('[data-start-form]');
        if (!form) return;
        form.addEventListener('submit', function (event) {
            event.preventDefault();
            var email = form.querySelector('input[type="email"]').value.trim();
            if (!email) return;
            var button = form.querySelector('button');
            button.disabled = true;
            send('POST', '/api/subscriptions', { email: email, tickers: [] })
                .then(function () {
                    window.location.href = '/link-sent?email=' + encodeURIComponent(email);
                })
                .catch(function (error) {
                    button.disabled = false;
                    showError(error.message);
                });
        });
    }

    function bindAccessForm() {
        var form = document.querySelector('[data-access-form]');
        if (!form) return;
        form.addEventListener('submit', function (event) {
            event.preventDefault();
            var email = form.querySelector('input[type="email"]').value.trim();
            if (!email) return;
            var button = form.querySelector('button');
            button.disabled = true;
            send('POST', '/auth/request-link', { email: email })
                .then(function () {
                    window.location.href = '/link-sent?email=' + encodeURIComponent(email);
                })
                .catch(function (error) {
                    button.disabled = false;
                    showError(error.message);
                });
        });
    }

    function bindResend() {
        var button = document.querySelector('[data-resend]');
        if (!button) return;
        button.addEventListener('click', function () {
            button.disabled = true;
            send('POST', '/auth/request-link', { email: button.getAttribute('data-resend') })
                .then(function () { button.textContent = 'Link sent'; })
                .catch(function (error) {
                    button.disabled = false;
                    showError(error.message);
                });
        });
    }

    function renderResults(list, results) {
        list.textContent = '';
        results.forEach(function (result) {
            var item = document.createElement('li');

            var ticker = document.createElement('span');
            ticker.className = 'ticker';
            ticker.textContent = result.ticker;

            var name = document.createElement('span');
            name.className = 'name';
            name.textContent = result.name;

            var action = document.createElement('button');
            action.className = 'btn outline';
            action.type = 'button';
            action.textContent = 'Subscribe';
            action.addEventListener('click', function () {
                action.disabled = true;
                send('POST', '/api/me/tickers', { ticker: result.ticker })
                    .then(function () { window.location.reload(); })
                    .catch(function (error) {
                        action.disabled = false;
                        showError(error.message);
                    });
            });

            item.appendChild(ticker);
            item.appendChild(name);
            item.appendChild(action);
            list.appendChild(item);
        });
    }

    function bindSearch() {
        var input = document.querySelector('[data-ticker-search]');
        var list = document.querySelector('[data-ticker-results]');
        if (!input || !list) return;

        var run = debounce(function (query) {
            if (query.length < 1) {
                list.textContent = '';
                return;
            }
            send('GET', '/api/companies?q=' + encodeURIComponent(query))
                .then(function (results) { renderResults(list, results || []); })
                .catch(function () { list.textContent = ''; });
        }, 180);

        input.addEventListener('input', function () { run(input.value.trim()); });
        document.addEventListener('click', function (event) {
            if (!input.parentElement.contains(event.target)) list.textContent = '';
        });
    }

    function bindRowActions() {
        document.querySelectorAll('[data-expand]').forEach(function (button) {
            button.addEventListener('click', function () {
                var panel = document.getElementById(button.getAttribute('data-expand'));
                if (!panel) return;
                var open = !panel.classList.contains('hidden');
                panel.classList.toggle('hidden', open);
                button.textContent = open ? '›' : '⌄';
                button.setAttribute('aria-expanded', String(!open));
            });
        });

        document.querySelectorAll('[data-remove]').forEach(function (button) {
            button.addEventListener('click', function () {
                var ticker = button.getAttribute('data-remove');
                if (button.getAttribute('data-confirm') !== 'yes') {
                    button.setAttribute('data-confirm', 'yes');
                    button.textContent = 'Remove?';
                    return;
                }
                button.disabled = true;
                send('DELETE', '/api/me/tickers/' + encodeURIComponent(ticker))
                    .then(function () { window.location.reload(); })
                    .catch(function (error) {
                        button.disabled = false;
                        showError(error.message);
                    });
            });
        });
    }

    function applyTheme(value) {
        document.documentElement.setAttribute('data-theme', value.toLowerCase());
    }

    function bindPreferences() {
        var groups = document.querySelectorAll('[data-pref-group]');
        if (!groups.length) return;
        groups.forEach(function (group) {
            var field = group.getAttribute('data-pref-group');
            group.querySelectorAll('[data-pref-value]').forEach(function (chip) {
                chip.addEventListener('click', function () {
                    var value = chip.getAttribute('data-pref-value');
                    group.querySelectorAll('[data-pref-value]').forEach(function (other) {
                        other.classList.toggle('on', other === chip);
                    });
                    if (field === 'appearance') applyTheme(value);
                    var payload = {};
                    payload[field] = value;
                    send('POST', '/api/me/preferences', payload)
                        .catch(function (error) { showError(error.message); });
                });
            });
        });

        document.querySelectorAll('[data-pref-toggle]').forEach(function (chip) {
            chip.addEventListener('click', function () {
                var field = chip.getAttribute('data-pref-toggle');
                var next = !chip.classList.contains('on');
                chip.classList.toggle('on', next);
                chip.textContent = chip.getAttribute('data-label') + (next ? ' · On' : ' · Off');
                var payload = {};
                payload[field] = next;
                send('POST', '/api/me/preferences', payload)
                    .catch(function (error) { showError(error.message); });
            });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        bindSignOut();
        bindStartForm();
        bindAccessForm();
        bindResend();
        bindSearch();
        bindRowActions();
        bindPreferences();
    });
})();
