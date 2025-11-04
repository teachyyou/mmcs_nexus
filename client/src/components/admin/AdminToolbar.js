// AdminToolbar.jsx
import { Toolbar } from '@mui/material';
import { SaveButton } from 'react-admin';
export default function AdminToolbar(props) {
    return (
        <Toolbar {...props} sx={{ px: 0, pt: 2 }}>
            <SaveButton label="Сохранить" variant="contained" color="primary" />
        </Toolbar>
    );
}
