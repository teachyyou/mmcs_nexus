import React from 'react';
import { AppBar, Button, Toolbar } from '@mui/material';
import { Link } from 'react-router-dom'; // Импортируем Link для навигации
import LoginButton from '../login/LoginButton';

const NavigationBar = ({ isAuthenticated, setIsAuthenticated }) => {
    return (
        <AppBar position='static' sx={{ height: 75 }}>
            <Toolbar>
                <Button component={Link} to="/admin" color="inherit">
                    Admin
                </Button>
                <Button component={Link} to="/grades" color="inherit">
                    Grades
                </Button>
                <LoginButton
                    className='login-button'
                    isAuthenticated={isAuthenticated}
                    setIsAuthenticated={setIsAuthenticated}
                />
            </Toolbar>
        </AppBar>
    );
};

export default NavigationBar;
