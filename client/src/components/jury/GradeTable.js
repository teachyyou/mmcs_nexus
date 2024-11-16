// GradeTable.js
import React, { useState, useEffect } from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, CircularProgress } from '@mui/material';
import GradeCard from './GradeCard';

const GradeTable = () => {
    const [grades, setGrades] = useState([]);
    const [projects, setProjects] = useState([]);
    const [juryMembers, setJuryMembers] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                // Здесь выполняются запросы к API для загрузки данных
                const [gradesRes, projectsRes, juryMembersRes] = await Promise.all([
                    fetch('/api/v1/grades').then((res) => res.json()),
                    fetch('/api/v1/projects').then((res) => res.json()),
                    fetch('/api/v1/jury-members').then((res) => res.json()),
                ]);

                setGrades(gradesRes);
                setProjects(projectsRes);
                setJuryMembers(juryMembersRes);
            } catch (error) {
                console.error("Ошибка загрузки данных:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    if (loading) {
        return <CircularProgress />;
    }

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
