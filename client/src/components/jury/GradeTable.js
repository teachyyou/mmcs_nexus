// GradeTable.js
import React from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper } from '@mui/material';
import GradeCard from './GradeCard';

const GradeTable = ({ grades, projects, juryMembers }) => {
    return (
        <TableContainer component={Paper}>
            <Table aria-label="grade table">
                <TableHead>
                    <TableRow>
                        <TableCell>Проекты / Жюри</TableCell>
                        {juryMembers.map((jury) => (
                            <TableCell key={jury.id} align="center">{jury.name}</TableCell>
                        ))}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {projects.map((project) => (
                        <TableRow key={project.id}>
                            <TableCell component="th" scope="row">
                                {project.name}
                            </TableCell>
                            {juryMembers.map((jury) => {
                                const grade = grades.find(
                                    (g) => g.projectId === project.id && g.juryId === jury.id
                                );
                                return (
                                    <TableCell key={jury.id} align="center">
                                        {grade ? <GradeCard grade={grade} /> : "N/A"}
                                    </TableCell>
                                );
                            })}
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );
};

export default GradeTable;
