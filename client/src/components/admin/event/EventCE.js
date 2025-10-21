// EventResource.js
import React from 'react';
import {
    Create,
    Edit,
    TopToolbar,
    useDataProvider,
    useRedirect,
    useResourceContext,
} from 'react-admin';
import { IconButton, Tooltip } from '@mui/material';
import ArrowBackIosNewIcon from '@mui/icons-material/ArrowBackIosNew';
import EventAdminForm from './EventAdminForm';

const EventActions = () => {
    const redirect = useRedirect();
    const resource = useResourceContext();

    return (
        <TopToolbar sx={{ justifyContent: 'flex-start', pl: 0, gap: 1 }}>
            <Tooltip title="Назад">
                <IconButton
                    onClick={() => redirect('list', resource)}
                    size="small"
                    aria-label="Назад"
                >
                    <ArrowBackIosNewIcon fontSize="small" />
                </IconButton>
            </Tooltip>
        </TopToolbar>
    );
};

export const EventCreate = (props) => {
    const dataProvider = useDataProvider();

    return (
        <Create title="Добавить событие" actions={<EventActions />} redirect="list" {...props}>
            <EventAdminForm requestMethod={dataProvider.create} />
        </Create>
    );
};

export const EventEdit = (props) => {
    const dataProvider = useDataProvider();

    return (
        <Edit
            title="Изменить событие"
            actions={<EventActions />}
            mutationMode="pessimistic"
            redirect="list"
            {...props}
        >
            <EventAdminForm requestMethod={dataProvider.update} />
        </Edit>
    );
};
