import React from 'react';
import {AppBar} from '@mui/material';
import LoginButton from '../login/LoginButton';

const NavigationBar = ({ isAuthenticated, setIsAuthenticated }) => {
    return (
        <AppBar position='static' sx={{
            height: 75,
        }}>
            <LoginButton
                className='login-button'
                isAuthenticated={isAuthenticated}
                setIsAuthenticated={setIsAuthenticated}
            />
        </AppBar>
    );
};

export default NavigationBar;
