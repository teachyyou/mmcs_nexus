// client/src/components/login/UpdateProfilePage.jsx
import React, { useState, useEffect, useMemo, useRef } from 'react'; // NEW: useRef
import { useNavigate } from 'react-router-dom';
import {
    Container, TextField, Button, Typography, Box,
    Paper, Stack, Divider, CircularProgress, useTheme,
    Snackbar, Alert, // NEW
} from '@mui/material';
import { useAuth } from '../../AuthContext';

const UpdateProfilePage = () => {
    const theme = useTheme();
    const navigate = useNavigate();
    const { user, setIsAuthenticated, setUserStatus, setUser } = useAuth();

    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName]   = useState('');
    const [email, setEmail]         = useState('');

    const [saving, setSaving]       = useState(false);
    const [emailTouched, setEmailTouched] = useState(false);

    // NEW: snackbar state + таймер для навигации
    const [successOpen, setSuccessOpen] = useState(false);
    const navTimerRef = useRef(null);

    const isSfeduEmail = (val) => /^[^\s@]+@sfedu\.ru$/i.test(val.trim());

    const emailInvalid = useMemo(() => !isSfeduEmail(email), [email]);

    useEffect(() => {
        if (!user) return;
        setFirstName(user.firstName ?? '');
        setLastName(user.lastName ?? '');
        setEmail(user.email ?? '');
    }, [user]);

    // NEW: очистка таймера при размонтировании
    useEffect(() => () => {
        if (navTimerRef.current) clearTimeout(navTimerRef.current);
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (emailInvalid) {
            setEmailTouched(true);
            return;
        }

        setSaving(true);
        try {
            const res = await fetch('/api/v1/auth/update_profile', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ firstName, lastName, email: email.trim() }),
            });

            if (res.ok) {
                let updatedUser = await res.json()
                setUserStatus('VERIFIED');
                setUser(prev => ({ ...prev, ...updatedUser }));

                // NEW: показать уведомление и перейти через 800 мс
                setSuccessOpen(true);
                navTimerRef.current = setTimeout(() => navigate('/'), 800);
            } else {
                console.error('Profile updating failed.');
            }
        } catch (e) {
            console.error('Profile updating error:', e);
        } finally {
            setSaving(false);
        }
    };

    const handleLogout = async () => {
        try {
            const res = await fetch('/api/v1/auth/logout', {
                method: 'POST',
                credentials: 'include',
            });
            if (!res.ok) throw new Error('Network response was not ok');
            setIsAuthenticated(false);
            navigate('/');
        } catch (e) {
            console.error('Logout failed:', e);
        }
    };

    return (
        <Container maxWidth="sm" sx={{ pb: 6 }}>
            <Paper
                elevation={0}
                sx={{
                    mt: 4, p: 3, borderRadius: 3,
                    border: `1px solid ${theme.palette.divider}`,
                    background: theme.palette.mode === 'dark'
                        ? theme.palette.background.paper
                        : '#fff',
                }}
            >
                <Box sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Typography variant="h5" sx={{ fontWeight: 700 }}>
                        Профиль
                    </Typography>
                    <Typography variant="body2" sx={{ opacity: 0.75, ml: 'auto' }}>
                        {theme.palette.mode === 'dark' ? 'Тёмная тема' : 'Светлая тема'}
                    </Typography>
                </Box>

                <Divider sx={{ mb: 3 }} />

                <Box component="form" onSubmit={handleSubmit} noValidate>
                    <Stack spacing={2}>
                        <TextField
                            label="Имя"
                            value={firstName}
                            onChange={(e) => setFirstName(e.target.value)}
                            fullWidth
                            required
                        />
                        <TextField
                            label="Фамилия"
                            value={lastName}
                            onChange={(e) => setLastName(e.target.value)}
                            fullWidth
                            required
                        />
                        <TextField
                            // NEW: тип, паттерн, ошибка и подсказка
                            type="email"
                            label="Почта (@sfedu.ru)"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            onBlur={() => setEmailTouched(true)}
                            fullWidth
                            required
                            inputProps={{ pattern: '^[^\\s@]+@sfedu\\.ru$' }}
                            error={emailTouched && emailInvalid}
                            helperText={
                                emailTouched && emailInvalid
                                    ? 'Используйте корпоративную почту, оканчивающуюся на @sfedu.ru'
                                    : ' '
                            }
                        />
                    </Stack>

                    <Stack
                        direction={{ xs: 'column', sm: 'row' }}
                        spacing={1.5}
                        sx={{ mt: 3 }}
                    >
                        <Button
                            type="submit"
                            variant="contained"
                            color="primary"
                            disabled={saving || emailInvalid}
                        >
                            {saving ? 'Сохранение…' : 'Сохранить'}
                        </Button>

                        <Box sx={{ flexGrow: 1 }} />

                        <Button
                            variant="outlined"
                            color="inherit"
                            onClick={handleLogout}
                            sx={{
                                borderColor:
                                    theme.palette.mode === 'dark'
                                        ? 'rgba(255,255,255,0.35)'
                                        : theme.palette.divider,
                            }}
                        >
                            Выйти
                        </Button>
                    </Stack>
                </Box>
            </Paper>

            {/* NEW: success snackbar */}
            <Snackbar
                open={successOpen}
                onClose={() => setSuccessOpen(false)}
                autoHideDuration={3000}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
            >
                <Alert
                    onClose={() => setSuccessOpen(false)}
                    severity="success"
                    variant="filled"
                    sx={{ width: '100%' }}
                >
                    Данные профиля успешно сохранены
                </Alert>
            </Snackbar>
        </Container>
    );
};

export default UpdateProfilePage;
