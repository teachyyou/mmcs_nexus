import React, { useState } from 'react';
import { Box, TextField, Typography } from '@mui/material';

const InlineGradeEditor = ({ gradeItem, onUpdate }) => {
    const [buildPoints, setBuildPoints] = useState(
        gradeItem.buildPoints !== undefined ? gradeItem.buildPoints : ''
    );
    const [presPoints, setPresPoints] = useState(
        gradeItem.presPoints !== undefined ? gradeItem.presPoints : ''
    );

    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
            <Box sx={{ border: '1px solid black', borderRadius: 1, p: 0.5, textAlign: 'center' }}>
                <Typography variant="caption">Build Points</Typography>
                <TextField
                    type="number"
                    variant="outlined"
                    size="small"
                    value={buildPoints}
                    onChange={(e) => setBuildPoints(e.target.value)}
                    onBlur={() => onUpdate('buildPoints', parseInt(buildPoints, 10))}
                    inputProps={{
                        style: { textAlign: 'center', fontSize: '0.75rem', padding: '2px' },
                        WebkitAppearance: 'none',
                        MozAppearance: 'textfield'
                    }}
                    sx={{ width: '100%' }}
                />
            </Box>
            <Box sx={{ border: '1px solid black', borderRadius: 1, p: 0.5, textAlign: 'center' }}>
                <Typography variant="caption">Presentation Points</Typography>
                <TextField
                    type="number"
                    variant="outlined"
                    size="small"
                    value={presPoints}
                    onChange={(e) => setPresPoints(e.target.value)}
                    onBlur={() => onUpdate('presPoints', parseInt(presPoints, 10))}
                    inputProps={{
                        style: { textAlign: 'center', fontSize: '0.75rem', padding: '2px' },
                        WebkitAppearance: 'none',
                        MozAppearance: 'textfield'
                    }}
                    sx={{ width: '100%' }}
                />
            </Box>
        </Box>
    );
};

export default InlineGradeEditor;
