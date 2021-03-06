/**
 * CommonFramework
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.tools.commonframework.standard.protex.report.template;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.tools.commonframework.standard.protex.report.model.TemplateColumn;
import com.blackducksoftware.tools.commonframework.standard.protex.report.model.TemplatePojo;
import com.blackducksoftware.tools.commonframework.standard.protex.report.model.TemplateSheet;

/**
 * Writes out rows to an existing Template created by the {@link TemplateReader } .
 * 
 * @author Ari Kamen
 * @param <T>
 *            This is the extension of the TemplatePojo
 */
public class TemplateWriter<T extends TemplatePojo> {

    /** The log. */
    private final Logger log = LoggerFactory.getLogger(this.getClass()
            .getName());

    /**
     * Flags to instruct the TemplateWriter to either ignore or proceed on mapping errors
     * Mapping errors are where the Pojo methods do not match to the columns
     */
    public static final Boolean IGNORE_MAPPING_ERRORS = true;

    // Indicator for Excel Workbook style setup - will be cloned and applied

    public static final Boolean WORKBOOK_STYLE_CLONE_TRUE = true;

    // Indicator for Excel Workbook style setup - will not be cloned and applied
    public static final Boolean WORKBOOK_STYLE_CLONE_FALSE = false;

    public static final Boolean EXIT_ON_MAPPING_ERRORS = false;

    /** The template reader. */
    private final TemplateReader templateReader;

    /** The pojo class. */
    private Class<T> pojoClass;

    /** The book. */
    private Workbook book;

    /**
     * Instantiates a new template writer.
     * 
     * @param templateReader
     *            the template reader
     */
    public TemplateWriter(TemplateReader templateReader) {
        this.templateReader = templateReader;
    }

    /**
     * Return the work book
     * 
     * @return the work book
     */
    public Workbook getWorkbook() {
        return book;
    }

    /**
     * Writes out pojos to specific sheet by using internal mappings specified
     * by the user and generates the workbook.
     * 
     * @param pojoList
     *            the pojo list
     * @param templateSheet
     *            the template sheet
     * @param pojoClass
     *            the pojo class
     * @param ignoreMissingColumns
     *            - if true, proceeds despite missing mappings (will write out what it can)
     * @throws Exception
     *             the exception
     */
    public void writeOutPojo(List<T> pojoList, TemplateSheet templateSheet,
            Class<T> pojoClass, boolean ignoreMissingColumns) throws Exception {

        writeOutPojo(pojoList,
                templateSheet,
                pojoClass,
                ignoreMissingColumns,
                IGNORE_MAPPING_ERRORS,
                WORKBOOK_STYLE_CLONE_TRUE);
    }

    /**
     * Writes out pojos to specific sheet by using internal mappings specified
     * by the user.
     * 
     * @param pojoList
     *            the pojo list
     * @param templateSheet
     *            the template sheet
     * @param pojoClass
     *            the pojo class
     * @param ignoreMissingColumns
     *            - if true, proceeds despite missing mappings (will write out what it can, if the generateWorkBook flag
     *            is set to true)
     * @param generateWorkBook
     *            - if true, proceeds with generating the workbook should be written out after the sheet processing is
     *            complete
     * @param cloneStyle
     *            - if true, apply the styles to the new cells
     * @throws Exception
     *             the exception
     */
    public void writeOutPojo(List<T> pojoList, TemplateSheet templateSheet,
            Class<T> pojoClass, boolean ignoreMissingColumns, boolean generateWorkBook, boolean cloneStyle) throws Exception {

        this.pojoClass = pojoClass;
        testReflectionMappings(templateSheet, pojoList.get(0), ignoreMissingColumns);

        book = templateReader.getInternalWorkBook();
        Map<String, TemplateColumn> columnMap = templateSheet.getColumnMap();

        Sheet activeSheet = book.getSheet(templateSheet.getSheetName());
        int i = 1;
        for (TemplatePojo pojo : pojoList) {
            Row activeRow = activeSheet.createRow(i);
            writePojoValuesToRow(activeSheet, activeRow, pojo, columnMap, cloneStyle);
            i++;
        }

        // Write the workbook if the flag is set to true
        if (generateWorkBook) {
            writeWorkBook(generateWorkBook);
        }
    }

    /**
     * Handles writing the pojo values to row for the provided sheet
     * 
     * @param pojoList
     *            the list of pojo values
     * @param templateSheet
     *            the template sheet
     * @param pojoClass
     *            the pojo class
     * @param ignoreMissingColumns
     *            - if true, proceeds despite missing mappings (will write out what it can)
     * @return the updated workbook
     * @throws Exception
     */
    public Workbook writeOutPojoValuesToSheet(List<T> pojoList, TemplateSheet templateSheet,
            Class<T> pojoClass, boolean ignoreMissingColumns) throws Exception {

        this.pojoClass = pojoClass;
        testReflectionMappings(templateSheet, pojoList.get(0), ignoreMissingColumns);

        book = templateReader.getInternalWorkBook();

        Map<String, TemplateColumn> columnMap = templateSheet.getColumnMap();

        Sheet activeSheet = book.getSheet(templateSheet.getSheetName());

        int i = 1;
        for (TemplatePojo pojo : pojoList) {
            Row activeRow = activeSheet.createRow(i);
            writePojoValuesToRow(activeSheet, activeRow, pojo, columnMap, WORKBOOK_STYLE_CLONE_TRUE);
            i++;
        }
        return book;
    }

    /**
     * Write pojo values to row for the provided sheet.
     * 
     * @param activeSheet
     *            the provided sheet for pojo values write out to the rows
     * @param activeRow
     *            the active row
     * @param pojo
     *            the pojo
     * @param columnMap
     *            the column map
     * @param cloneStyle
     *            - if true, apply the styles to the new cells
     */
    private void writePojoValuesToRow(Sheet activeSheet,
            Row activeRow,
            TemplatePojo pojo,
            Map<String, TemplateColumn> columnMap,
            boolean cloneStyle) {

        Iterator<String> it = columnMap.keySet().iterator();
        while (it.hasNext())
        {
            String key = it.next();
            TemplateColumn column = columnMap.get(key);
            Integer position = column.getColumnPos();
            CellStyle styleFromTemplate = column.getCellStyle();

            Cell activeCell;
            int cellType = column.getCellType();
            if (cellType == Cell.CELL_TYPE_FORMULA) {
                activeCell = activeRow.createCell(position, Cell.CELL_TYPE_FORMULA);
                log.debug("Active Cell is PartOfArrayFormulaGroup: " + activeCell.isPartOfArrayFormulaGroup());
            }
            else if (cellType == Cell.CELL_TYPE_NUMERIC) {
                activeCell = activeRow.createCell(position, Cell.CELL_TYPE_NUMERIC);
            }
            else {
                activeCell = activeRow.createCell(position, Cell.CELL_TYPE_STRING);
            }

            // Set the value
            String pojoValue = getValueFromPojo(pojo, column.getLookupMappingName());
            activeCell.setCellValue(pojoValue);

            // Set the cell style
            // TODO: This catches the XML Disconnected exception, but the styles come out all wrong on subsequent
            // sheets.
            // Appears to only happen in the unit tests.
            if (cloneStyle) {
                try {
                    CellStyle newcs = book.createCellStyle();
                    newcs.cloneStyleFrom(styleFromTemplate);
                    activeCell.setCellStyle(newcs);
                } catch (Exception e)
                {
                    log.warn("Unable to copy cell styles!" + e.getMessage());
                }
            }

            if (cellType == Cell.CELL_TYPE_FORMULA) {
                copyFormula(activeSheet, activeCell, activeRow, column);
            }
        }
    }

    /**
     * Handles copying the formula for the provided sheet and active row
     * 
     * @param sheet
     *            the provided sheet
     * @param targetCell
     *            the target cell to copy the formula
     * @param activeRow
     *            the active row
     * @param column
     *            the TemplateColumn to be used for
     */
    private void copyFormula(Sheet sheet, Cell targetCell, Row activeRow, TemplateColumn column) {
        if (targetCell == null || sheet == null ||
                targetCell.getCellType() != Cell.CELL_TYPE_FORMULA) {
            return;
        }

        String formula = column.getCellFormula();

        int shiftRows = activeRow.getRowNum() - 1;
        int shiftCols = 0;

        XSSFEvaluationWorkbook workbookWrapper =
                XSSFEvaluationWorkbook.create((XSSFWorkbook) sheet.getWorkbook());

        Ptg[] ptgs = FormulaParser.parse(formula,
                workbookWrapper,
                FormulaType.CELL,
                sheet.getWorkbook().getSheetIndex(sheet));

        for (Ptg ptg : ptgs) {
            if (ptg instanceof RefPtgBase)
            {
                RefPtgBase ref = (RefPtgBase) ptg;
                if (ref.isColRelative()) {
                    ref.setColumn(ref.getColumn() + shiftCols);
                }
                if (ref.isRowRelative()) {
                    ref.setRow(ref.getRow() + shiftRows);
                }
            } else if (ptg instanceof AreaPtg)
            {
                AreaPtg ref = (AreaPtg) ptg;
                if (ref.isFirstColRelative()) {
                    ref.setFirstColumn(ref.getFirstColumn() + shiftCols);
                }
                if (ref.isLastColRelative()) {
                    ref.setLastColumn(ref.getLastColumn() + shiftCols);
                }
                if (ref.isFirstRowRelative()) {
                    ref.setFirstRow(ref.getFirstRow() + shiftRows);
                }
                if (ref.isLastRowRelative()) {
                    ref.setLastRow(ref.getLastRow() + shiftRows);
                }
            }
        }

        formula = FormulaRenderer.toFormulaString(workbookWrapper, ptgs);
        targetCell.setCellFormula(formula);
        log.debug("Set Formula for row " + activeRow.getRowNum() + " : " + formula);
        targetCell.setAsActiveCell();
    }

    /**
     * Write work book.
     * 
     * @throws Exception
     *             the exception
     */
    public void writeWorkBook() throws Exception {
        writeWorkBook(true);
    }

    /**
     * Write work book.
     * 
     * @param workbookUpdatePeriodic
     *            indicator for workbook periodic update after each sheet processing
     * @throws Exception
     *             the exception
     */
    public void writeWorkBook(boolean workbookUpdatePeriodic) throws Exception {

        FileOutputStream stream = null;
        File outputLocation = templateReader.getOutputLocation();

        try {
            if (outputLocation != null) {
                stream = new FileOutputStream(outputLocation);
            } else {
                throw new Exception("Unable to write, destination unknown!");
            }

            book.write(stream);
            log.info("Finished writing workbook to: " + outputLocation);

            // Relead the book every time, in case multiple sheets need to be
            // written to.
            // POI [Bug 49940]
            // https://issues.apache.org/bugzilla/show_bug.cgi?id=49940
            if (workbookUpdatePeriodic) {
                TemplateReader.generateWorkBookFromFile(outputLocation);
            }

        } catch (Exception e) {
            log.error("Fatal: " + e.getMessage());
            throw new Exception("Fatal error, unable to write out workbook:", e);
        } finally {
            try {
                stream.close();
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * Tests to make sure that the mappings properly map to the POJO.
     * 
     * @param templateSheet
     *            the template sheet
     * @param pojo
     *            the pojo
     * @param ignoreMissingColumns
     * @throws Exception
     *             If Mappings are wrong
     */
    private void testReflectionMappings(TemplateSheet templateSheet, T pojo, boolean ignoreMissingColumns)
            throws Exception {
        boolean missingMappings = false;
        boolean missingPojoMethods = false;

        List<String> missingNames = new ArrayList<String>();
        List<String> missingMethods = new ArrayList<String>();

        Collection<TemplateColumn> columns = templateSheet.getColumnMap()
                .values();
        for (TemplateColumn column : columns) {
            String lookupName = column.getLookupMappingName();
            if (lookupName == null) {
                missingMappings = true;
                missingNames.add(column.getColumnName());

            }
            String value = getValueFromPojo(pojo, lookupName);
            if (value == null) {
                missingPojoMethods = true;
                missingMethods.add(lookupName);
                log.error("Missing method: lookupName = " + lookupName + " for column " + column.getColumnName());
            }

        }

        if (missingMappings) {
            Collections.sort(missingNames);
            String prettyListStr = getPrettyList(missingNames);
            if (ignoreMissingColumns) {
                log.debug("Proceeding with writing, despite missing mappings for: " + prettyListStr);
            } else {
                throw new Exception("Columns missing mapping: " + prettyListStr);
            }
        }
        if (missingPojoMethods) {
            Collections.sort(missingMethods);
            String prettyListStr = getPrettyList(missingMethods);
            if (ignoreMissingColumns) {
                log.debug("Proceeding with writing, despite missing methods: " + prettyListStr);
            } else {
                throw new Exception("Methods missing from POJO: " + prettyListStr);
            }
        }

    }

    /**
     * Gets the pretty list.
     * 
     * @param list
     *            the list
     * @return the pretty list
     */
    private String getPrettyList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String s = it.next();
            sb.append(s);

            if (it.hasNext()) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    /**
     * Use reflection to derive the value from the POJO.
     * 
     * @param pojo
     *            the pojo
     * @param lookupMappingName
     *            the lookup mapping name
     * @return the value from pojo
     */
    private String getValueFromPojo(TemplatePojo pojo, String lookupMappingName) {
        String value = null;
        String lookUpMethodName = "get" + lookupMappingName;

        try {
            // Use method instead of declared to handle any class extension that may take place.
            Method method = pojo.getClass().getMethod(lookUpMethodName);
            pojo.getClass().getDeclaredMethods();
            if (method == null) {
                log.warn("Method for the pojo class does not exist with name: "
                        + lookUpMethodName);
                return value;
            }

            Object ret = method.invoke(pojo);
            value = (ret == null) ? "" : ret.toString();

        } catch (Exception e) {
            log.warn("Unable to reflectively get value for method: " + lookupMappingName + " - POJO: " + pojo.getClass());
        }

        return value;
    }

    /**
     * Print out the memory usage information
     */
    public void getMemoryInformation()
    {
        ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        log.debug("Heap:" + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
        log.debug("NonHeap:" + ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
        List<MemoryPoolMXBean> beans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean bean : beans) {
            log.debug(bean.getName() + ":" + bean.getUsage());
        }

        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            log.debug(bean.getName() + ":" + bean.getCollectionCount() + ":" + bean.getCollectionTime());
        }
    }
}
