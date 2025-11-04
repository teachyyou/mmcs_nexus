import React from 'react';
import { Container } from '@mui/material';

export default function Page({ children }) {
    return (
        <Container maxWidth="lg" sx={{ py: 4 }}>
            {children}
        </Container>
    );
}
