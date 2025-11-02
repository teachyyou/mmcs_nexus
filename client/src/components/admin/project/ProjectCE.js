// ProjectResource.js
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
import ProjectAdminForm from './ProjectAdminForm';

const ProjectActions = () => {
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

export const ProjectCreate = (props) => {
    const dataProvider = useDataProvider();

    return (
        <Create
            title="Добавить проект"
            actions={<ProjectActions />}   // 👈 стрелка сверху
            redirect="list"
            {...props}
        >
            <ProjectAdminForm requestMethod={dataProvider.create} />
        </Create>
    );
};

export const ProjectEdit = (props) => {
    const dataProvider = useDataProvider();

    return (
        <Edit
            title="Изменить проект"
            actions={<ProjectActions />}    // 👈 стрелка сверху
            mutationMode="pessimistic"
            redirect="list"
            {...props}
        >
            <ProjectAdminForm requestMethod={dataProvider.update} />
        </Edit>
    );
};
