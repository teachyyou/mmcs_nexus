import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, TextField, Button, Typography, Box } from '@mui/material';

const UpdateProfilePage = () => {
    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [course, setCourse] = useState("");
    const [userGroup, setUserGroup] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async (event) => {
        event.preventDefault();

        const response = await fetch('http://localhost:8080/api/v1/auth/update-profile', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify({
                firstName,
                lastName,
                course,
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
                        value={course}
                        onChange={(e) => setCourse(e.target.value)}
                        required
                    />
                    <TextField
                        label="Group"
                        variant="outlined"
                        type="number"
                        value={userGroup}
                        onChange={(e) => setUserGroup(e.target.value)}
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
