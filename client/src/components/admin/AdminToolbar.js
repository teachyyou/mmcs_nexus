import {SaveButton} from "react-admin";
import {Toolbar} from "@mui/material";
import React from "react";

const AdminToolbar = (props) => (
    <Toolbar {...props}>
        <SaveButton label="Сохранить" />
    </Toolbar>
);

export default AdminToolbar;