import React from 'react';
import Button from '@mui/material/Button';
import './LoginButton.css';

const LoginButton = ({ handleLogin, buttonText }) => {
    return (
        <Button
            className="login-button"
            onClick={handleLogin}
        >
            {buttonText}
        </Button>
    );
};

export default LoginButton;
