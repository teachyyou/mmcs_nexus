import React from 'react';
import { AppBar, Button, Toolbar } from '@mui/material';
import { Link } from 'react-router-dom';
import LoginButton from '../login/LoginButton';
import { useAuth, useIsAdmin, useIsJury } from '../../AuthContext';

const NavigationBar = () => {
    const { isAuthenticated, setIsAuthenticated } = useAuth();
    const isAdmin = useIsAdmin();
    const isJury = useIsJury();

    return (
        <AppBar position="static" sx={{ height: 75 }}>
            <Toolbar>
                {isAdmin && (
                    <Button component={Link} to="/admin" color="inherit">
                        Админка
                    </Button>
                )}
                {isJury && (
                    <Button component={Link} to="/grades" color="inherit">
                        Оценки
                    </Button>
                )}
                <LoginButton
                    className="login-button"
                    isAuthenticated={isAuthenticated}
                    setIsAuthenticated={setIsAuthenticated}
                />
            </Toolbar>
        </AppBar>
    );
};

export default NavigationBar;
