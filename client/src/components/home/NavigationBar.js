import React, { useState } from 'react';
import { AppBar, Toolbar, Button, IconButton, Avatar, Box } from '@mui/material';
import { Link } from 'react-router-dom'
import { useAuth, useIsAdmin, useIsJury } from '../../AuthContext';
import UserMenu from './UserMenu';

const NavigationBar = ({ avatarUrl, userName, userEmail }) => {
    const { isAuthenticated, setIsAuthenticated, user } = useAuth();
    const isAdmin = useIsAdmin();
    const isJury = useIsJury();

    const [menuOpen, setMenuOpen] = useState(false);

    const handleAvatarClick = () => {
        setMenuOpen(true);
    };

    const handleMenuClose = () => {
        setMenuOpen(false);
    };

    const handleLogout = async () => {
        await fetch('/api/v1/auth/logout', {
            method: 'POST',
            credentials: 'include'
        });
        setIsAuthenticated(false);
    };

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
                <Box sx={{ flexGrow: 1 }} />
                {isAuthenticated ? (
                    <>
                        <IconButton onClick={handleAvatarClick} sx={{ p: 0 }}>
                            <Avatar src={user.avatarUrl} alt={user.name} />
                        </IconButton>
                        <UserMenu open={menuOpen} onClose={handleMenuClose} user={user} onLogout={handleLogout} />
                    </>
                ) : (
                    <Button component={Link} to='/oauth2/authorization/github' color="inherit">
                        Вход
                    </Button>
                )}
            </Toolbar>
        </AppBar>
    );
};

export default NavigationBar;
