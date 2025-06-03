import React from 'react';
import Button from '@mui/material/Button';
import './LoginButton.css';
import {useNavigate} from 'react-router-dom';
import {StyledEngineProvider} from "@mui/material";
import { useAuth } from '../../AuthContext';



const LoginButton = () => {
    const navigate = useNavigate();
    const {isAuthenticated, setIsAuthenticated} = useAuth();


    const handleLogin = () => {
        navigate('/login');
    };

    const handleLogout = () => {
        fetch('http://localhost:8080/api/v1/auth/logout', {
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
