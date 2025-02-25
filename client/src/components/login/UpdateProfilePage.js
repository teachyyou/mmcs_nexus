import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, TextField, Button, Typography, Box } from '@mui/material';

const UpdateProfilePage = () => {
    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [userCourse, setCourse] = useState("");
    const [userGroup, setGroup] = useState("");
    const navigate = useNavigate();

    // Запрос к API для получения информации о пользователе
    useEffect(() => {
        const fetchUserInfo = async () => {
            try {
                const response = await fetch('http://localhost:8080/api/v1/auth/user', {
                    credentials: 'include',
                });
                if (response.ok) {
                    const data = await response.json();
                    // Если профиль уже заполнен, заполняем поля формы
                    if (data.firstname) setFirstName(data.firstname);
                    if (data.lastname) setLastName(data.lastname);
                    if (data.course) setCourse(data.course.toString());
                    if (data.group) setGroup(data.group.toString());
                } else {
                    console.error('Failed to fetch user info');
                }
            } catch (error) {
                console.error('Error fetching user info:', error);
            }
        };

        fetchUserInfo();
    }, []);

    const handleSubmit = async (event) => {
        event.preventDefault();

        const response = await fetch('http://localhost:8080/api/v1/auth/update_profile', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify({
                firstName,
                lastName,
                userCourse: parseInt(userCourse),
                userGroup: parseInt(userGroup),
            }),
        });

        if (response.ok) {
            navigate('/');
        } else {
            console.error('Profile updating failed.');
        }
    };

    return (
        <Container maxWidth="sm">
            <Box
                sx={{
                    border: '2px solid #1976d2',
                    borderRadius: '8px',
                    padding: 3,
                    mt: 5,
                    textAlign: 'center',
                }}
            >
                <Typography variant="h5" component="h1" gutterBottom>
                    Complete your profile
                </Typography>
                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                    <TextField
                        label="First Name"
                        variant="outlined"
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                        required
                    />
                    <TextField
                        label="Last Name"
                        variant="outlined"
                        value={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                        required
                    />
                    <TextField
                        label="Course"
                        variant="outlined"
                        value={userCourse}
                        onChange={(e) => setCourse(e.target.value)}
                        required
                    />
                    <TextField
                        label="Group"
                        variant="outlined"
                        type="number"
                        value={userGroup}
                        onChange={(e) => setGroup(e.target.value)}
                        required
                    />
                    <Button type="submit" variant="contained" color="primary">
                        Submit
                    </Button>
                </form>
            </Box>
        </Container>
    );
};

export default UpdateProfilePage;
