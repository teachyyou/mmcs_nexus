// client/src/components/home/NavigationBar.jsx
import React from 'react';
import {
    AppBar, Toolbar, Button, IconButton, Avatar, Box, Tooltip, Menu, MenuItem, Divider, useMediaQuery,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import LightModeIcon from '@mui/icons-material/LightMode';
import { Link, useLocation } from 'react-router-dom';
import { useAuth, useIsAdmin, useIsJury } from '../../AuthContext';
import { useSidebarState } from 'react-admin';
import { useThemeMode } from '../../ThemeModeProvider';

const LEFT_SLOT_W = 48;        // ширина слота под гамбургер (иконка ≈40px + отступ)
const CENTER_MIN_W = 220;      // минимум под «Админка/Оценки», чтобы не прыгал логотип
const RIGHT_SLOT_MIN_W = 160;  // минимум под переключатель темы + аватар/вход

export default function NavigationBar() {
    const { isAuthenticated, setIsAuthenticated, user } = useAuth();
    const isAdmin = useIsAdmin();
    const isJury = useIsJury();
    const isNarrow = useMediaQuery('(max-width: 600px)');
    const { pathname } = useLocation();
    const { mode, toggleMode } = useThemeMode();
    const dark = mode === 'dark';

    const [anchorEl, setAnchorEl] = React.useState(null);
    const menuOpen = Boolean(anchorEl);

    const handleAvatarClick = (e) => setAnchorEl(e.currentTarget);
    const handleMenuClose = () => setAnchorEl(null);
    const handleLogout = async () => {
        try { await fetch('/api/v1/auth/logout', { method: 'POST', credentials: 'include' }); } catch {}
        setIsAuthenticated(false);
        handleMenuClose();
    };

    const onAdmin = pathname.startsWith('/admin');

    const emitToggleSidebar = () => {
        if (onAdmin) {
            window.dispatchEvent(new CustomEvent('ra-toggle-sidebar'));
        }
    };

    return (
        <AppBar
            position="fixed"
            color={dark ? 'primary' : 'inherit'}
            sx={{
                height: 64,
                justifyContent: 'center',
                zIndex: (t) => t.zIndex.drawer + 1,
                ...(dark
                    ? { boxShadow: '0 2px 12px rgba(91,91,214,0.25)', color: '#fff' }
                    : { borderBottom: '1px solid #EAEAF0' }),
                transition: 'background-color 240ms ease, color 240ms ease, border-color 240ms ease, box-shadow 240ms ease',
            }}
        >
            <Toolbar disableGutters sx={{ px: 2, gap: 8/8, color: 'inherit', minHeight: 64, display: 'flex', alignItems: 'center' }}>
                {/* ЛЕВЫЙ СЛОТ: фиксированное место под гамбургер */}
                <Box sx={{ width: LEFT_SLOT_W, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, mr: 1 }}>
                    <IconButton
                        edge="start"
                        aria-label="menu"
                        onClick={emitToggleSidebar}
                        sx={{
                            color: 'inherit',
                            opacity: onAdmin ? 1 : 0,
                            transform: onAdmin ? 'translateX(0)' : 'translateX(-8px)',
                            transition: 'opacity 200ms ease, transform 200ms ease',
                            pointerEvents: onAdmin ? 'auto' : 'none',
                        }}
                    >
                        <MenuIcon />
                    </IconButton>
                </Box>

                {/* ЦЕНТРАЛЬНЫЙ БЛОК: логотип + «Админка/Оценки» с минимальной шириной */}
                <Box
                    sx={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 1,
                        minWidth: isNarrow ? 0 : CENTER_MIN_W, // на мобилках разрешаем сжиматься
                        flexGrow: 1,
                    }}
                >
                    <Box component={Link} to="/" sx={{ textDecoration: 'none', color: 'inherit', fontWeight: 700, fontSize: 18, whiteSpace: 'nowrap' }}>
                        MMCS Nexus
                    </Box>

                    {/* Кнопки всегда смонтированы, меняем только видимость */}
                    <Button
                        component={Link}
                        to="/admin"
                        color="inherit"
                        sx={{
                            ml: 2,
                            opacity: isAuthenticated && isAdmin ? 1 : 0,
                            pointerEvents: isAuthenticated && isAdmin ? 'auto' : 'none',
                            transition: 'opacity 180ms ease',
                            whiteSpace: 'nowrap',
                        }}
                    >
                        {!isNarrow && 'Админка'}
                    </Button>

                    <Button
                        component={Link}
                        to="/grades"
                        color="inherit"
                        sx={{
                            opacity: isAuthenticated && isJury ? 1 : 0,
                            pointerEvents: isAuthenticated && isJury ? 'auto' : 'none',
                            transition: 'opacity 180ms ease',
                            whiteSpace: 'nowrap',
                        }}
                    >
                        {!isNarrow && 'Оценки'}
                    </Button>
                </Box>

                {/* ПРАВЫЙ СЛОТ: фиксированный минимум ширины, чтобы ничего не ездило */}
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', minWidth: RIGHT_SLOT_MIN_W, flexShrink: 0, ml: 1 }}>
                    <Tooltip title={dark ? 'Светлая тема' : 'Тёмная тема'}>
                        <IconButton onClick={toggleMode} size="small" aria-label="toggle theme" sx={{ color: 'inherit' }}>
                            {dark ? <LightModeIcon /> : <DarkModeIcon />}
                        </IconButton>
                    </Tooltip>

                    {isAuthenticated ? (
                        <>
                            <IconButton onClick={handleAvatarClick} sx={{ ml: 1, color: 'inherit' }}>
                                <Avatar
                                    src={user?.avatarUrl}
                                    alt={user?.githubName || user?.login || 'user'}
                                    sx={{
                                        width: 36,
                                        height: 36,
                                        boxShadow: dark ? '0 0 0 2px rgba(255,255,255,0.6)' : '0 0 0 2px rgba(91,91,214,0.15)',
                                    }}
                                />
                            </IconButton>

                            <Menu
                                anchorEl={anchorEl}
                                open={menuOpen}
                                onClose={handleMenuClose}
                                disableScrollLock
                                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                                transformOrigin={{ vertical: 'top', horizontal: 'right' }}
                                ModalProps={{ keepMounted: true }}
                                PaperProps={{ sx: { mt: 1.25, borderRadius: 2, minWidth: 220, maxWidth: 'calc(100vw - 16px)' } }}
                                MenuListProps={{ dense: true }}
                            >
                                <Box sx={{ px: 2, py: 1.5, maxWidth: 260 }}>
                                    <Box sx={{ fontWeight: 600 }}>{user?.githubName || user?.login}</Box>
                                    <Box sx={{ opacity: 0.7, fontSize: 12 }}>{user?.email}</Box>
                                </Box>
                                <Divider />
                                <MenuItem
                                    component={Link}
                                    to="/update_profile"
                                    onClick={handleMenuClose}
                                >
                                    Профиль
                                </MenuItem>
                                <MenuItem onClick={handleLogout}>Выйти</MenuItem>
                            </Menu>
                        </>
                    ) : (
                        <Button
                            color={dark ? 'inherit' : 'primary'}
                            variant={dark ? 'outlined' : 'contained'}
                            onClick={() => window.location.assign('/oauth2/authorization/github')}
                            sx={{ ml: 1, ...(dark && { borderColor: 'rgba(255,255,255,0.75)' }) }}
                        >
                            Войти
                        </Button>
                    )}
                </Box>
            </Toolbar>
        </AppBar>
    );
}
