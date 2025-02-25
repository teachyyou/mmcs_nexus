import React from 'react';
import { AppBar, UserMenu, TitlePortal } from 'react-admin';
import { Box, Button, Typography } from '@mui/material';
import { Link } from 'react-router-dom';

const CustomAppBar = (props) => (
    <AppBar {...props}>
        <TitlePortal>
            <Typography variant="h6" color="inherit" id="react-admin-title" />
        </TitlePortal>

        <Box sx={{ flex: 1 }} />

        {/* Дополнительные кнопки */}
        <Button component={Link} to="/" color="inherit">
            Главная
        </Button>
        <Button component={Link} to="/grades" color="inherit">
            Оценки
        </Button>

        {/* Стандартное меню пользователя */}
        <UserMenu {...props} />
    </AppBar>
);

export default CustomAppBar;
