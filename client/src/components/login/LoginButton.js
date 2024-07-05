import React from 'react';
import Button from '@mui/material/Button';
import './LoginButton.css';

const LoginButton = ({ handleLogin }) => {
    return (
        <Button
            className="login-button"
            onClick={handleLogin}
        >
            Login
        </Button>
    );
};

export default LoginButton;
