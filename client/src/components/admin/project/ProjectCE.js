import React from 'react';
import {Create, Edit, useDataProvider} from 'react-admin';
import ProjectAdminForm from "./ProjectAdminForm";

export const ProjectCreate = (props) => {
    const dataProvider = useDataProvider();

    return (
        <Create title="Добавить проект" {...props} redirect="list">
            <ProjectAdminForm requestMethod={dataProvider.create}/>
        </Create>
    );
};

export const ProjectEdit = (props) => {
    const dataProvider = useDataProvider();

    return (
        <Edit title="Изменить проект" {...props} redirect="list">
            <ProjectAdminForm requestMethod={dataProvider.update}/>
        </Edit>
    );
};
