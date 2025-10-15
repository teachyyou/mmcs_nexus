// client/src/components/login/UpdateProfilePage.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Container,
    TextField,
    Button,
    Typography,
    Box,
    Paper,
    Stack,
    Divider,
    CircularProgress,
    useTheme,
} from '@mui/material';
import { useAuth } from '../../AuthContext';

const UpdateProfilePage = () => {
    const theme = useTheme();
    const navigate = useNavigate();
    const { setIsAuthenticated, setUserStatus } = useAuth();

    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName]   = useState('');
    const [email, setEmail]   = useState('');
    const [loading, setLoading]     = useState(true);
    const [saving, setSaving]       = useState(false);

    useEffect(() => {
        const fetchUserInfo = async () => {
            try {
                const res = await fetch('/api/v1/auth/user', { credentials: 'include' });
                if (res.ok) {
                    const data = await res.json();
                    if (data.firstname) setFirstName(data.firstname);
                    if (data.lastname)  setLastName(data.lastname);
                    if (data.email)  setEmail(data.email);
                } else {
                    console.error('Failed to fetch user info');
                }
            } catch (e) {
                console.error('Error fetching user info:', e);
            } finally {
                setLoading(false);
            }
        };
        fetchUserInfo();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);
        try {
            const res = await fetch('/api/v1/auth/update_profile', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ firstName, lastName, email }),
            });

            if (res.ok) {
                setUserStatus('VERIFIED');
                navigate('/');
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
                    mt: 4,
                    p: 3,
                    borderRadius: 3,
                    border: `1px solid ${theme.palette.divider}`,
                    background:
                        theme.palette.mode === 'dark'
                            ? theme.palette.background.paper
                            : '#fff',
                }}
            >
                <Box
                    sx={{
                        mb: 2,
                        display: 'flex',
                        alignItems: 'center',
                        gap: 1,
                    }}
                >
                    <Typography variant="h5" sx={{ fontWeight: 700 }}>
                        Профиль
                    </Typography>
                    <Typography
                        variant="body2"
                        sx={{ opacity: 0.75, ml: 'auto' }}
                    >
                        {theme.palette.mode === 'dark' ? 'Тёмная тема' : 'Светлая тема'}
                    </Typography>
                </Box>

                <Divider sx={{ mb: 3 }} />

                {loading ? (
                    <Box sx={{ py: 6, display: 'flex', justifyContent: 'center' }}>
                        <CircularProgress />
                    </Box>
                ) : (
                    <Box component="form" onSubmit={handleSubmit}>
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
                                label="Почта"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                fullWidth
                                required
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
                                disabled={saving}
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
                )}
            </Paper>
        </Container>
    );
};

export default UpdateProfilePage;
