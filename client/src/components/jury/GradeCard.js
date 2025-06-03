// GradeCard.js
import React from 'react';
import { Card, CardContent, Typography } from '@mui/material';

const GradeCard = ({ grade }) => {
    return (
        <Card variant="outlined" sx={{ minWidth: 150, margin: 1 }}>
            <CardContent>
                <Typography variant="h6" component="div">
                    {grade.projectName || "Без названия"}
                </Typography>
                <Typography color="text.secondary">
                    Презентация: {grade.presPoints ?? "N/A"}
                </Typography>
                <Typography color="text.secondary">
                    Сборка: {grade.buildPoints ?? "N/A"}
                </Typography>
                <Typography variant="body2">
                    Комментарий: {grade.comment ?? "N/A"}
                </Typography>
            </CardContent>
        </Card>
    );
};

export default GradeCard;
