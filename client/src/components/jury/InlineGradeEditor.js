import React, { useState, useEffect } from 'react';
import { Box, TextField, Typography, InputAdornment, Stack } from '@mui/material';
import { useAuth } from "../../AuthContext";

const labelSx = { fontSize: 11, fontWeight: 600, opacity: 0.7, textTransform: 'uppercase', letterSpacing: 0.2 };

const fieldSx = {
    '& .MuiOutlinedInput-root': {
        height: 36,
        fontSize: 14,
        textAlign: 'center',
    },
    '& input': {
        textAlign: 'center',
        padding: '6px 8px',
    },
    width: 140,
};

const InlineGradeEditor = ({ gradeItem, maxBuild, maxPres, onUpdate }) => {
    const [buildPoints, setBuildPoints] = useState(
        gradeItem.buildPoints !== undefined && gradeItem.buildPoints !== null ? gradeItem.buildPoints : ''
    );
    const [presPoints, setPresPoints] = useState(
        gradeItem.presPoints !== undefined && gradeItem.presPoints !== null ? gradeItem.presPoints : ''
    );

    useEffect(() => {
        setBuildPoints(gradeItem.buildPoints !== null ? gradeItem.buildPoints : '');
        setPresPoints(gradeItem.presPoints !== null ? gradeItem.presPoints : '');
    }, [gradeItem]);

    const { userId } = useAuth();
    const isOwner = userId === gradeItem.juryId;

    const handleUpdate = async (field, value) => {
        const numericValue = value === '' ? '' : parseInt(value, 10);
        const result = await onUpdate(field, numericValue);
        if (result && !result.success) {
            if (field === 'buildPoints') setBuildPoints(result.oldValue);
            if (field === 'presPoints') setPresPoints(result.oldValue);
        }
    };

    return (
        <Stack spacing={0.5} alignItems="center">
            <Box>
                <Typography sx={labelSx}>Презентация</Typography>
                <TextField
                    type="number"
                    variant="outlined"
                    size="small"
                    value={presPoints}
                    onChange={(e) => setPresPoints(e.target.value)}
                    onBlur={() => isOwner && handleUpdate('presPoints', presPoints)}
                    disabled={!isOwner}
                    inputProps={{ min: 0, max: maxPres }}
                    InputProps={{
                        endAdornment: <InputAdornment position="end">/{maxPres}</InputAdornment>,
                    }}
                    sx={fieldSx}
                />
            </Box>

            <Box>
                <Typography sx={labelSx}>Билд</Typography>
                <TextField
                    type="number"
                    variant="outlined"
                    size="small"
                    value={buildPoints}
                    onChange={(e) => setBuildPoints(e.target.value)}
                    onBlur={() => isOwner && handleUpdate('buildPoints', buildPoints)}
                    disabled={!isOwner}
                    inputProps={{ min: 0, max: maxBuild }}
                    InputProps={{
                        endAdornment: <InputAdornment position="end">/{maxBuild}</InputAdornment>,
                    }}
                    sx={fieldSx}
                />
            </Box>
        </Stack>
    );
};

export default InlineGradeEditor;
