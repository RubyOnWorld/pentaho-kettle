/*
 * Ruby for pentaho kettle
 * Copyright (C) 2017 Twineworks GmbH
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.twineworks.kettle.ruby.step.streams;

import com.twineworks.kettle.ruby.step.RubyStepData;
import com.twineworks.kettle.ruby.step.execmodels.SimpleExecutionModel;
import org.jruby.runtime.builtin.IRubyObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStep;

import java.util.LinkedList;
import java.util.List;

public class ErrorStreamWriter {

  private BaseStep step;
  private SimpleExecutionModel model;
  private RubyStepData data;
  private List<Object[]> rowList;
  private List<Object[]> errRowList;
  private int rowSize;
  private int errorSize;
  private RowMetaInterface inRow;

  private int idxErrorCount;
  private int idxErrorField;
  private int idxErrorDesc;
  private int idxErrorCode;

  public ErrorStreamWriter(SimpleExecutionModel model) throws KettleStepException {

    this.model = model;
    this.step = model.getStep();
    this.data = model.getData();
    this.rowList = new LinkedList<Object[]>();
    this.errRowList = new LinkedList<Object[]>();

    rowSize = data.inputRowMeta.size() + data.errorRowMeta.size();

    idxErrorCount = data.errorRowMeta.indexOfValue(data.stepErrorMeta.getNrErrorsValuename());
    idxErrorField = data.errorRowMeta.indexOfValue(data.stepErrorMeta.getErrorFieldsValuename());
    idxErrorDesc = data.errorRowMeta.indexOfValue(data.stepErrorMeta.getErrorDescriptionsValuename());
    idxErrorCode = data.errorRowMeta.indexOfValue(data.stepErrorMeta.getErrorCodesValuename());

    inRow = new RowMeta();

  }

  public void write(IRubyObject rubyOut) throws KettleException {

    Object[] r = new Object[rowSize];

    rowList.clear();
    model.fetchRowsFromScriptOutput(rubyOut, inRow, r, rowList, data.inputRowMeta.getValueMetaList(), data.inputRowMeta);

    errRowList.clear();
    model.fetchRowsFromScriptOutput(rubyOut, inRow, new Object[errorSize], errRowList, data.errorRowMeta.getValueMetaList(), data.errorRowMeta);

    int i = 0;
    for (Object[] outRow : rowList) {
      Object[] er = errRowList.get(i);
      step.putError(data.inputRowMeta, outRow,
        idxErrorCount >= 0 ? (Long) er[idxErrorCount] : -1,
        idxErrorDesc >= 0 ? (String) er[idxErrorDesc] : null,
        idxErrorField >= 0 ? (String) er[idxErrorField] : null,
        idxErrorCode >= 0 ? (String) er[idxErrorCode] : null
      );
      i++;
    }

  }

}
