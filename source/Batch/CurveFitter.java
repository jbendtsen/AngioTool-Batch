    public double[] doLinearFit() {
		int cleanPoints = 0;
		for (int jj=0; jj<xData.length; jj++) {
			if (!Double.isNaN(xData[jj] + yData[jj]))
				cleanPoints++;
		}
		if (cleanPoints==xData.length) {
			this.xData = xData;
			this.yData = yData;
		} else { //remove pairs containing a NaN
			double[] cleanX = new double[cleanPoints];
			double[] cleanY = new double[cleanPoints];
			int ptr = 0;
			for (int jj=0; jj<xData.length; jj++) {
				if (!Double.isNaN(xData[jj] + yData[jj])) {
					cleanX[ptr] = xData[jj];
					cleanY[ptr] = yData[jj];
					ptr++;
				}
			}
			this.xData = cleanX;
			this.yData = cleanY;
		}
		numPoints = this.xData.length;
        calculateSumYandY2();
        finalParams = new double[] {0, 0, 0};
        doRegression(finalParams);
        return finalParams;
    }

	/** calculates the sum of y and y^2 (weighted sum if we have weights) */
	private void calculateSumYandY2() {
		sumY = 0.0; sumY2 = 0.0; sumWeights = 0.0;
		double w = 1.0;
		for (int i=0; i<numPoints; i++) {
			double y = yData[i];
			if (weights != null) w = weights[i];
			sumY += y*w;
			sumY2 += y*y*w;
			sumWeights += w;
		}
	}

    /** Determine sum of squared residuals with linear regression.
	 *	The sum of squared residuals is written to the array element with index 'numParams',
	 *	the offset and factor params (if any) are written to their proper positions in the
	 *	params array */
	private void doRegression(double[] params) {
		double sumX=0, sumX2=0, sumXY=0; //sums for regression; here 'x' are function values
		double sumY=0, sumY2=0;			//only calculated for 'slope', otherwise we use the values calculated already
		double sumWeights=0;
		for (int i=0; i<numPoints; i++) {
			double fValue = fitType == STRAIGHT_LINE ? 0 : f(params, xData[i]);	 // function value
			if (Double.isNaN(fValue)) { //check for NaN now; later we need NaN checking for division-by-zero check.
				params[numParams] = Double.NaN;
				return;					//sum of squared residuals is NaN if any value is NaN
			}
			double w = weights==null ? 1 : weights[i];
			sumWeights += w;
			//if(getIterations()==0)IJ.log(xData[i]+"\t"+yData[i]+"\t"+fValue); //x,y,function
			double x = xData[i];
			double y = yData[i] - fValue;
			sumX += x*w;
			sumX2 += x*x*w;
			sumXY += x*y*w;
			sumY2 += y*y*w;
			sumY += y*w;
		}

		double factor = 0; // factor or slope
		double sumResidualsSqr = 0;
		// full linear regression or offset only. Slope is named 'factor' here
		factor = (sumXY-sumX*sumY/sumWeights)/(sumX2-sumX*sumX/sumWeights);
		else if (Double.isNaN(factor) || Double.isInfinite(factor))
			factor = 0;			// all 'x' values are equal, any factor (slope) will fit
		double offset = (sumY-factor*sumX)/sumWeights;
		params[offsetParam] = offset;
		sumResidualsSqr = sqr(factor)*sumX2 + sumWeights*sqr(offset) + sumY2 +
				2*factor*offset*sumX - 2*factor*sumXY - 2*offset*sumY;
		// check for accuracy problem: large difference of small numbers?
		// Don't report unrealistic or even negative values, otherwise minimization could lead
		// into parameters where we have a numeric problem
		if (sumResidualsSqr < 2e-15*(sqr(factor)*sumX2 + sumWeights*sqr(offset) + sumY2))
			sumResidualsSqr = 2e-15*(sqr(factor)*sumX2 + sumWeights*sqr(offset) + sumY2);
		//if(){IJ.log("sumX="+sumX+" sumX2="+sumX2+" sumXY="+sumXY+" factor="+factor+" offset=="+offset);}
		params[numParams] = sumResidualsSqr;
		params[factorParam] = factor;
	}
