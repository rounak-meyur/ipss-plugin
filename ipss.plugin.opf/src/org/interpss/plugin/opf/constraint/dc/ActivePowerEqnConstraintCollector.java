package org.interpss.plugin.opf.constraint.dc;

import java.util.List;

import org.interpss.plugin.opf.OpfSolverFactory;
import org.interpss.plugin.opf.constraint.OpfConstraint;
import org.interpss.plugin.opf.util.OpfDataHelper;

import com.interpss.core.aclf.AclfBus;
import com.interpss.core.net.Bus;
import com.interpss.opf.BaseOpfBranch;
import com.interpss.opf.BaseOpfBus;
import com.interpss.opf.BaseOpfNetwork;
import com.interpss.opf.cst.OpfConstraintType;
import com.interpss.opf.cst.impl.BaseOpfConstraintContainerImpl;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

public class ActivePowerEqnConstraintCollector extends BaseOpfConstraintContainerImpl {	
	
	SparseDoubleMatrix2D Y = null;		
	
	public ActivePowerEqnConstraintCollector(BaseOpfNetwork<?, ?> opfNet) {
		this.setNetwork(opfNet);	
		Y =  OpfDataHelper.getBusAdmittance(opfNet);
	}			
	
	@Override
	public void collectConstraint() {		
		int bracnt = 0;		
		int genIdx = 0;
		for (Bus b : opfNet.getBusList()) {	
			
			OpfConstraint cst = new OpfConstraint();				
			SparseDoubleMatrix1D yrow =  (SparseDoubleMatrix1D) Y.viewRow(bracnt++);
			IntArrayList col_0 = new IntArrayList();
			DoubleArrayList val_0 = new DoubleArrayList();
			yrow.getNonZeros(col_0, val_0);
			int size = col_0.size();			
			
			IntArrayList colNo = new IntArrayList();
			DoubleArrayList val = new DoubleArrayList();
			
			double pl = 0;	
			AclfBus bus = (AclfBus)b;
			if (opfNet.isOpfGenBus(b)) {
				//colNo.add(b.getSortNumber());
				colNo.add(genIdx++);
				val.add(1);				
			}
			if (bus.isLoad()){
				pl = bus.getLoadP();								
			}		
			for(int i = 0; i<size;i++){
				colNo.add(col_0.get(i)+this.numOfGen);
				double value = val_0.get(i)*(-1);
				value = OpfDataHelper.round(value,5);
				val.add(value);				
			}			
			
			int id = cstContainer.size();
			String des = "PBalance@"+ b.getId();
			double UpperLimit = pl;
			double LowerLimit = pl;			
			
			cst = OpfSolverFactory.createOpfConstraint(id, des, UpperLimit, LowerLimit, OpfConstraintType.EQUALITY, colNo, val);
			cstContainer.add(cst);			
		}			
	}
	

}
