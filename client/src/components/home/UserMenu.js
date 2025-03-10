import React from 'react';
import {
    Paper,
    Box,
    Avatar,
    Typography,
    Divider,
    MenuItem,
    Link as MuiLink,
    useTheme,
    IconButton,
    Grow,
    ClickAwayListener
} from '@mui/material';
import { Link } from 'react-router-dom';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';

const UserMenu = ({ open, onClose, user, onLogout }) => {
    const theme = useTheme();
    const menuWidth = 280;

    if (!open) return null;

    return (
        <div>
            <ClickAwayListener onClickAway={onClose}>
                <div>
                    <Grow in={open} style={{ transformOrigin: 'top right' }}>
                        <Paper
                            sx={{
                                position: 'fixed',
                                top: 0,
                                right: 0,
                                width: menuWidth,
                                backgroundColor: theme.palette.primary.main,
                                color: theme.palette.primary.contrastText,
                                border: `1px solid ${theme.palette.primary.dark}`,
                                zIndex: theme.zIndex.appBar + 1,
                                // Срезанный нижний левый угол (20px)
                                clipPath: 'polygon(0 0, 100% 0, 100% 100%, 20px 100%, 0 calc(100% - 20px))',
                            }}
                        >
                            {/* Кнопка закрытия в левом верхнем углу заменена на стрелочку вправо */}
                            <Box sx={{ position: 'absolute', top: 4, left: 4 }}>
                                <IconButton onClick={onClose} size="small" sx={{ color: theme.palette.primary.contrastText }}>
                                    <ArrowForwardIosIcon fontSize="small" />
                                </IconButton>
                            </Box>
                            <Box sx={{ pt: 4, p: 2, textAlign: 'center' }}>
                                <Avatar
                                    src={user.avatarUrl || 'https://via.placeholder.com/80'}
                                    sx={{ width: 80, height: 80, mb: 1, mx: 'auto' }}
                                />
                                <MuiLink
                                    component={Link}
                                    to="/profile"
                                    underline="none"
                                    color="inherit"
                                    onClick={onClose}
                                >
                                    <Typography variant="h6">{user.name || 'User Name'}</Typography>
                                </MuiLink>
                                {user.email && (
                                    <Typography variant="body2" color="inherit">
                                        {user.email}
                                    </Typography>
                                )}
                            </Box>
                            <Divider sx={{ bgcolor: theme.palette.primary.dark }} />
                            <MenuItem component={Link} to="/profile" onClick={onClose} sx={{ color: 'inherit' }}>
                                Профиль
                            </MenuItem>
                            <MenuItem component={Link} to="/settings" onClick={onClose} sx={{ color: 'inherit' }}>
                                Настройки
                            </MenuItem>
                            <MenuItem onClick={() => { onLogout(); onClose(); }} sx={{ color: 'inherit' }}>
                                Выйти
                            </MenuItem>
                        </Paper>
                    </Grow>
                </div>
            </ClickAwayListener>
        </div>
    );
};

export default UserMenu;
