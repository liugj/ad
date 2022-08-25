package com.example;

/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener;
import org.apache.poi.hssf.eventusermodel.*;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 用sax解析xls 格式文档 转成csv格式
 */
public class XLS2CSV implements HSSFListener {
    private final POIFSFileSystem fs;
    // 存储行记录的容器
    private final List<String> rows = new ArrayList<>();

    //Excel数据
    private final List<ArrayList<String>> data = new ArrayList<>();
    /**
     * Should we output the formula, or the value it has?
     */
    private final boolean outputFormulaValues = false;
   
    /**
     * For parsing Formulas
     */
    private SheetRecordCollectingListener workbookBuildingListener;
    private HSSFWorkbook stubWorkbook;
    // Records we pick up as we process
    private SSTRecord sstRecord;
    private FormatTrackingHSSFListener formatListener;
    // private BoundSheetRecord[] orderedBSRs;
    private boolean outputNextStringRecord;

    /**
     * Creates a new XLS -> CSV converter
     *
     * @param fs         The POIFSFileSystem to process
     * @param minColumns The minimum number of columns to output, or -1 for no minimum
     */
    public XLS2CSV(POIFSFileSystem fs, int minColumns) {
        this.fs = fs;
    }

    /**
     * Creates a new XLS -> CSV converter
     *
     * @param is         The file to process
     * @param minColumns The minimum number of columns to output, or -1 for no minimum
     */
    public XLS2CSV(InputStream is, int minColumns) throws IOException {
        this(
                new POIFSFileSystem(is),
                minColumns
        );
    }

    public static void main(String[] args) throws Exception {
        XLS2CSV xls2csv = new XLS2CSV(Files.newInputStream(Paths.get("E:\\workspace\\java\\card-xls.xls")), 20);
        xls2csv.process();
        List<ArrayList<String>> data2 = xls2csv.getData();
        for (ArrayList<String> arrayList : data2) {
            System.out.println(arrayList.toString());
        }

    }

    public List<ArrayList<String>> getData() {
        return data;
    }

    /**
     * Initiates the processing of the XLS file to CSV
     */
    public void process() throws IOException {
        MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
        formatListener = new FormatTrackingHSSFListener(listener);

        HSSFEventFactory factory = new HSSFEventFactory();
        HSSFRequest request = new HSSFRequest();

        if (outputFormulaValues) {
            request.addListenerForAllRecords(formatListener);
        } else {
            workbookBuildingListener = new SheetRecordCollectingListener(formatListener);
            request.addListenerForAllRecords(workbookBuildingListener);
        }

        factory.processWorkbookEvents(request, fs);
    }

    /**
     * Main HSSFListener method, processes events, and outputs the
     * CSV as the file is processed.
     */
    @Override
    public void processRecord(Record record) {
        int thisColumn;
        String thisStr = null;
        String value;


        // 当前行
        switch (record.getSid()) {
            //---------add start---------
            case FontRecord.sid://字体记录
                break;
            case FormatRecord.sid://单元格样式记录
                /*FormatRecord format = (FormatRecord) record;*/
                break;
            case ExtendedFormatRecord.sid://扩展单元格样式记录
                break;
            //---------add end---------
            case BOFRecord.sid://type=5为workbook的开始
                BOFRecord br = (BOFRecord) record;
                if (br.getType() == BOFRecord.TYPE_WORKSHEET) {
                    // 如果有需要，则建立子工作薄
                    if (workbookBuildingListener != null && stubWorkbook == null) {
                        stubWorkbook = workbookBuildingListener.getStubHSSFWorkbook();
                    }

                }
                break;
            case SSTRecord.sid://存储了xls所有文本单元格值，通过索引获取
                sstRecord = (SSTRecord) record;
                break;

            case BlankRecord.sid:
                BlankRecord blankRecord = (BlankRecord) record;
                thisColumn = blankRecord.getColumn();
                thisStr = "";
                rows.add(thisColumn, thisStr);
                //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
                break;
            case BoolErrRecord.sid: // 单元格为布尔类型
                BoolErrRecord boolErrRecord = (BoolErrRecord) record;
                thisColumn = boolErrRecord.getColumn();
                thisStr = boolErrRecord.getBooleanValue() + "";
                rows.add(thisColumn, thisStr);
                //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
                break;

            case FormulaRecord.sid: // 单元格为公式类型
                FormulaRecord formulaRecord = (FormulaRecord) record;
                thisColumn = formulaRecord.getColumn();
                if (outputFormulaValues) {
                    if (Double.isNaN(formulaRecord.getValue())) {
                        // Formula result is a string
                        // This is stored in the next record
                        outputNextStringRecord = true;
                        // For handling formulas with string results
                    } else {
                        thisStr = formatListener.formatNumberDateCell(formulaRecord);
                    }
                } else {
                    thisStr = '"' + HSSFFormulaParser.toFormulaString(stubWorkbook, formulaRecord.getParsedExpression()) + '"';
                }
                rows.add(thisColumn, thisStr);
                //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
                break;
            case StringRecord.sid:// 单元格中公式的字符串
                if (outputNextStringRecord) {
                    // String for formula
                    outputNextStringRecord = false;
                }
                break;
            case LabelRecord.sid:
                LabelRecord labelRecord = (LabelRecord) record;
                thisColumn = labelRecord.getColumn();
                value = labelRecord.getValue().trim();
                value = value.equals("") ? " " : value;
                this.rows.add(thisColumn, value);
                //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
                break;
            case LabelSSTRecord.sid: // 单元格为字符串类型
                LabelSSTRecord labelSSTRecord = (LabelSSTRecord) record;
                thisColumn = labelSSTRecord.getColumn();
                if (sstRecord == null) {
                    rows.add(thisColumn, " ");
                    //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
                } else {
                    value = sstRecord.getString(labelSSTRecord.getSSTIndex()).toString().trim();
                    value = value.equals("") ? " " : value;
                    rows.add(thisColumn, value);
                    //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
                }
                break;
            case NumberRecord.sid: // 单元格为数字类型
                NumberRecord numberRecord = (NumberRecord) record;
                thisColumn = numberRecord.getColumn();
                value = formatListener.formatNumberDateCell(numberRecord).trim();
                value = value.equals("") ? " " : value;
                // 向容器加入列值
                rows.add(thisColumn, value);
                //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
                break;

            case EOFRecord.sid:
            default:
                break;
        }

        // 遇到新行的操作


        // 空值的操作
        if (record instanceof MissingCellDummyRecord) {
            MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
            thisColumn = mc.getColumn();
            rows.add(thisColumn, " ");
            //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
        }
        // 更新行和列的值

        // 行结束时的操作
        if (record instanceof LastCellOfRowDummyRecord) {
            ArrayList<String> list = new ArrayList<>(rows);
            data.add(list);
            // 清空容器
            rows.clear();
        }
    }
}
