import React, { useState } from 'react';
import { Box, TextField, Typography, InputAdornment } from '@mui/material';

const InlineGradeEditor = ({ gradeItem, maxBuild, maxPres, onUpdate }) => {
    const [buildPoints, setBuildPoints] = useState(
        gradeItem.buildPoints !== undefined ? gradeItem.buildPoints : ''
    );
    const [presPoints, setPresPoints] = useState(
        gradeItem.presPoints !== undefined ? gradeItem.presPoints : ''
    );

    const handleUpdate = async (field, value) => {
        const numericValue = value === '' ? '' : parseInt(value, 10);
        const result = await onUpdate(field, numericValue);

        if (result && !result.success) {
            if (field === 'buildPoints') {
                setBuildPoints(result.oldValue);
            } else if (field === 'presPoints') {
                setPresPoints(result.oldValue);
            }
        }
    };

    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
            <Box sx={{ border: '1px solid black', borderRadius: 1, p: 0.5, textAlign: 'center' }}>
                <Typography variant="caption">Презентация</Typography>
                <TextField
                    type="number"
                    variant="outlined"
                    size="small"
                    value={presPoints}
                    onChange={(e) => setPresPoints(e.target.value)}
                    onBlur={() => handleUpdate('presPoints', presPoints)}
                    InputProps={{
                        style: { textAlign: 'center', fontSize: '0.75rem', padding: '2px' },
                        endAdornment: (
                            <InputAdornment position="end">
                                /{maxPres}
                            </InputAdornment>
                        )
                    }}
                    sx={{ width: '100%' }}
                />
            </Box>
            <Box sx={{ border: '1px solid black', borderRadius: 1, p: 0.5, textAlign: 'center' }}>
                <Typography variant="caption">Билд</Typography>
                <TextField
                    type="number"
                    variant="outlined"
                    size="small"
                    value={buildPoints}
                    onChange={(e) => setBuildPoints(e.target.value)}
                    onBlur={() => handleUpdate('buildPoints', buildPoints)}
                    InputProps={{
                        style: { textAlign: 'center', fontSize: '0.75rem', padding: '2px' },
                        endAdornment: (
                            <InputAdornment position="end">
                                /{maxBuild}
                            </InputAdornment>
                        )
                    }}
                    sx={{ width: '100%' }}
                />
            </Box>
        </Box>
    );
};

export default InlineGradeEditor;
