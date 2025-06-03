import React, { useState } from 'react';
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Box,
    Link as MuiLink,
    Card, Tooltip
} from '@mui/material';
import { Link } from 'react-router-dom';
import GradeEditorModal from './GradeEditorModal';
import InlineGradeEditor from './InlineGradeEditor';
import { useAuth } from "../../AuthContext";

const cellSx = {
    border: '2px solid black',
    p: 0.5,
    maxWidth: 120,
    minHeight: 60,
    overflow: 'hidden'
};

const GradeTable = ({ grades, event, onGradeUpdated }) => {
    const [selectedGrade, setSelectedGrade] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedJuryId, setSelectedJuryId] = useState(null);
    const [selectedProjectId, setSelectedProjectId] = useState(null);
    const { userId } = useAuth();

    const handleCellClick = (grade, projectId, juryId) => {
        setSelectedGrade(grade);
        setSelectedJuryId(juryId);
        setSelectedProjectId(projectId);
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setSelectedGrade(null);
        setSelectedJuryId(null);
        setSelectedProjectId(null);
    };

    const handleInlineUpdate = async (gradeItem, field, newValue) => {
        const oldValue = gradeItem[field];
        const updatedGrade = { ...gradeItem, [field]: newValue };
        try {
            const response = await fetch('/api/v1/jury/grades', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(updatedGrade)
            });
            if (!response.ok) {
                const errorData = await response.json();
                console.error('Ошибка при сохранении оценки:', errorData);
            }
            const updatedGradeFromServer = await response.json();
            onGradeUpdated(updatedGradeFromServer);
        } catch (error) {
            return { success: false, oldValue };
        }
        return { success: true };
    };

    // Словарь для быстрого доступа к оценкам
    const gradeMap = {};
    grades.rows.forEach(row => {
        gradeMap[row.projectId] = {};
        row.tableRow.forEach(item => {
            gradeMap[row.projectId][item.juryId] = item;
        });
    });

    return (
        <>
            <TableContainer component={Card} sx={{ mt: 2 }}>
                <Table sx={{ minWidth: 600, tableLayout: 'flexible', width: '100%' }}>
                    <TableHead>
                        <TableRow>
                            <TableCell sx={{ ...cellSx, fontWeight: 'bold' }}>
                                Проекты / Жюри
                            </TableCell>
                            {grades.juries.map(jury => (
                                <TableCell key={jury.id} align="center" sx={{ ...cellSx, fontWeight: 'bold' }}>
                                    {jury.firstName} {jury.lastName}
                                </TableCell>
                            ))}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {grades.rows.map(({ projectId, mentorId, tableRow }) => {
                            const project = grades.projects.find(p => p.id === projectId);
                            return (
                                <TableRow key={projectId}>
                                    <TableCell sx={{ ...cellSx, fontWeight: 'bold' }}>
                                        {project?.name}
                                    </TableCell>
                                    {grades.juries.map(jury => {
                                        const gradeItem = gradeMap[projectId]?.[jury.id];
                                        const isOwner = jury.id === userId;
                                        const isMentorCell = jury.id === mentorId;
                                        if (isMentorCell) {
                                            return (
                                                <TableCell key={jury.id} align="center" sx={cellSx}>
                                                    <Tooltip title="Ментор не имеет права оценивать свои проекты">
                                                        <span>
                                                        <   Box sx={{ color: '#999' }}>Недоступно</Box>
                                                        </span>
                                                    </Tooltip>
                                                </TableCell>
                                            );
                                        }
                                        return (
                                            <TableCell key={jury.id} align="center" sx={cellSx}>
                                                {gradeItem ? (
                                                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                                                        <InlineGradeEditor
                                                            gradeItem={gradeItem}
                                                            maxBuild={event.maxBuildPoints}
                                                            maxPres={event.maxPresPoints}
                                                            onUpdate={(field, newValue) =>
                                                                handleInlineUpdate(gradeItem, field, newValue)
                                                            }
                                                        />
                                                        <MuiLink
                                                            component={Link}
                                                            to="#"
                                                            onClick={() => handleCellClick(gradeItem, projectId, jury.id)}
                                                            sx={{ fontSize: '0.75rem', color: 'blue' }}
                                                        >
                                                            Подробнее...
                                                        </MuiLink>
                                                    </Box>
                                                ) : (
                                                    isOwner ? (
                                                        <MuiLink
                                                            component={Link}
                                                            to="#"
                                                            onClick={() => handleCellClick(null, projectId, jury.id)}
                                                            sx={{ fontSize: '0.75rem', color: 'blue' }}
                                                        >
                                                            Не оценено
                                                        </MuiLink>
                                                    ) : (
                                                        <Box sx={{ fontSize: '0.75rem', color: '#999' }}>
                                                            Не оценено
                                                        </Box>
                                                    )
                                                )}
                                            </TableCell>
                                        );
                                    })}
                                </TableRow>
                            );
                        })}
                    </TableBody>
                </Table>
            </TableContainer>
            <GradeEditorModal
                open={isModalOpen}
                onClose={handleCloseModal}
                grade={selectedGrade}
                projectId={selectedProjectId}
                juryId={selectedJuryId}
                grades={grades}
                onGradeUpdated={onGradeUpdated}
            />
        </>
    );
};

export default GradeTable;
