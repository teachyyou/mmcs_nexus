import React, { useState } from 'react';
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Box,
    Paper,
    Tooltip,
    Button,
} from '@mui/material';
import { Link } from 'react-router-dom';
import GradeEditorModal from './GradeEditorModal';
import InlineGradeEditor from './InlineGradeEditor';
import { useAuth } from "../../AuthContext";

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
    };

    const handleDialogExited = () => {
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
                body: JSON.stringify(updatedGrade),
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

    // Ускоряем доступ к оценкам
    const gradeMap = {};
    grades.rows.forEach(row => {
        gradeMap[row.projectId] = {};
        row.tableRow.forEach(item => {
            gradeMap[row.projectId][item.juryId] = item;
        });
    });

    return (
        <>
            <TableContainer
                component={Paper}
                elevation={1}
                sx={{
                    mt: 2,
                    borderRadius: 2,
                    overflow: 'auto',
                    border: theme => `1px solid ${theme.palette.divider}`,
                }}
            >
                <Table size="small" sx={{ minWidth: 900 }}>
                    <TableHead>
                        <TableRow
                            sx={{
                                '& th': {
                                    borderColor: theme => theme.palette.divider,
                                },
                            }}
                        >
                            <TableCell
                                sx={{
                                    fontWeight: 700,
                                    position: 'sticky',
                                    left: 0,
                                    zIndex: theme => theme.zIndex.appBar,
                                    background: theme => theme.palette.background.paper,
                                    minWidth: 240,
                                }}
                            >
                                Проекты / Жюри
                            </TableCell>

                            {grades.juries.map(jury => (
                                <TableCell
                                    key={jury.id}
                                    align="center"
                                    sx={{
                                        fontWeight: 700,
                                        whiteSpace: 'nowrap',
                                    }}
                                >
                                    {jury.firstName} {jury.lastName}
                                </TableCell>
                            ))}
                        </TableRow>
                    </TableHead>

                    <TableBody
                        sx={{
                            '& td': { borderColor: theme => theme.palette.divider },
                            '& tr:nth-of-type(odd)': {
                                backgroundColor: theme => theme.palette.action.hover,
                            },
                            '& tr:hover': {
                                backgroundColor: theme => theme.palette.action.selected,
                                transition: 'background-color 160ms ease',
                            },
                        }}
                    >
                        {grades.rows.map(({ projectId, mentorId }) => {
                            const project = grades.projects.find(p => p.id === projectId);

                            return (
                                <TableRow key={projectId}>
                                    <TableCell
                                        sx={{
                                            fontWeight: 600,
                                            position: 'sticky',
                                            left: 0,
                                            zIndex: theme => theme.zIndex.appBar,
                                            background: theme => theme.palette.background.paper,
                                            minWidth: 240,
                                            whiteSpace: 'nowrap',
                                        }}
                                    >
                                        {project?.name}
                                    </TableCell>

                                    {grades.juries.map(jury => {
                                        const gradeItem = gradeMap[projectId]?.[jury.id];
                                        const isOwner = jury.id === userId;
                                        const isMentorCell = jury.id === mentorId;

                                        if (isMentorCell) {
                                            return (
                                                <TableCell key={jury.id} align="center" sx={{ color: 'text.disabled', verticalAlign: 'middle' }}>
                                                    <Tooltip title="Ментор не имеет права оценивать свои проекты">
                                                        <span>Недоступно</span>
                                                    </Tooltip>
                                                </TableCell>
                                            );
                                        }

                                        return (
                                            <TableCell key={jury.id} align="center" sx={{ verticalAlign: 'middle' }}>
                                                {gradeItem ? (
                                                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 0.5 }}>
                                                        <InlineGradeEditor
                                                            gradeItem={gradeItem}
                                                            maxBuild={event.maxBuildPoints}
                                                            maxPres={event.maxPresPoints}
                                                            onUpdate={(field, newValue) =>
                                                                handleInlineUpdate(gradeItem, field, newValue)
                                                            }
                                                        />

                                                        <Button
                                                            component={Link}
                                                            to="#"
                                                            onClick={() => handleCellClick(gradeItem, projectId, jury.id)}
                                                            size="small"
                                                            variant="text"
                                                            sx={{ mt: 0.25, display: 'inline-flex', mx: 'auto' }}
                                                        >
                                                            Подробнее
                                                        </Button>
                                                    </Box>
                                                ) : (
                                                    isOwner ? (
                                                        <Button
                                                            component={Link}
                                                            to="#"
                                                            onClick={() => handleCellClick(null, projectId, jury.id)}
                                                            size="small"
                                                            variant="text"
                                                            sx={{ display: 'inline-flex', mx: 'auto' }}
                                                        >
                                                            <Tooltip title="Проект ещё не был оценён">
                                                                <span>Не оценено</span>
                                                            </Tooltip>
                                                        </Button>
                                                    ) : (
                                                        <Tooltip title="Проект ещё не был оценён">
                                                            <Box
                                                                component="span"
                                                                sx={{
                                                                    display: 'inline-flex',
                                                                    alignItems: 'center',
                                                                    justifyContent: 'center',
                                                                    minHeight: 24,
                                                                    px: 0.5,
                                                                    fontSize: '0.8rem',
                                                                    color: 'text.disabled',
                                                                }}
                                                            >
                                                                Не оценено
                                                            </Box>
                                                        </Tooltip>
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
                onExited={handleDialogExited}
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
