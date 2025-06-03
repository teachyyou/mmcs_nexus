import React from 'react';
import Button from '@mui/material/Button';

//сейчас не используется, войти можно сразу с панели навигации
const LoginPage = () => {
    const handleLogin = (provider) => {
        window.location.href = `/oauth2/authorization/${provider}`;
    };

    return (
        <div style={{ textAlign: 'center', marginTop: '50px' }}>
            <h1>Login with OAuth2</h1>
            <Button
                variant="contained"
                color="primary"
                onClick={() => handleLogin('github')}
                style={{ margin: '10px' }}
            >
                Login with GitHub
            </Button>
            {/* Add other providers if needed */}
        </div>
    );
};

export default LoginPage;
