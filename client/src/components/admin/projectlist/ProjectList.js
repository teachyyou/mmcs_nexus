import React from 'react';
import { List, Datagrid, TextField } from 'react-admin';

const ProjectList = (props) => {
    return (
        <List {...props}>
            <Datagrid>
                <TextField source="id" />
                <TextField source="name" label="Name" />
                <TextField source="description" label="Description" />
                <TextField source="type" label="Type" />
                <TextField source="year" label="Year" />
            </Datagrid>
        </List>
    );
}

export default ProjectList;
