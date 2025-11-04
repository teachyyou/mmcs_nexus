import React from 'react';
import { Container, Paper, Skeleton, Stack } from '@mui/material';

export default function LoadingCard() {
    return (
        <Container maxWidth="lg" sx={{ py: 4 }}>
            <Paper sx={{ p: 3 }}>
                <Stack spacing={2}>
                    <Skeleton variant="text" width={220} height={32} />
                    <Skeleton variant="rectangular" height={140} />
                    <Skeleton variant="text" width="60%" />
                </Stack>
            </Paper>
        </Container>
    );
}
