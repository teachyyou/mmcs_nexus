import React from 'react';
import Button from '@mui/material/Button';
import './LoginButton.css';
import {useNavigate} from 'react-router-dom';
import {StyledEngineProvider} from "@mui/material";

const LoginButton = ({ isAuthenticated, setIsAuthenticated }) => {
    const navigate = useNavigate();

    const handleLogin = () => {
        navigate('/login');
    };

    const handleLogout = () => {
        fetch('http://localhost:8080/logout', {
            method: 'POST',
            credentials: 'include'
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                setIsAuthenticated(false);
                navigate('/');
            })

    };

    return (
        <StyledEngineProvider injectFirst>
            <Button
                className="login-button"
                onClick={isAuthenticated ? handleLogout : handleLogin}
            >
                {isAuthenticated ? "Logout" : "Login"}
            </Button>
        </StyledEngineProvider>
    );
};

export default LoginButton;
