package be.uantwerpen.idlab.tasksetgenerator.taskCreator;

import java.util.ArrayList;
import java.util.List;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;


public class LpSolver {
	private List<WCET> programs;

       
	public LpSolver(List<WCET> programs) 
	{
		this.programs = programs;
	}
	//Not usable if there exist a minimum n for each benchmark program
	/*public List<WCET> findProgramCombinationB(double wantedExecTime) 
	{
	   	glp_prob lp;
        glp_iocp iocp;
        SWIGTYPE_p_int ind;
        SWIGTYPE_p_double val;
        int ret;

        List<WCET> combinationOfPrograms = new ArrayList<WCET>();
        WCET wcet;
              
		try {
            // Create problem
            lp = GLPK.glp_create_prob();
            GLPK.glp_set_prob_name(lp, "findWCET");
            GLPK.glp_add_cols(lp, programs.size());
            
            for(int i=0;i<programs.size();i++)
            {
            	 GLPK.glp_set_col_name(lp, i+1, "x"+i);
                 GLPK.glp_set_col_kind(lp, i+1, GLPKConstants.GLP_IV);
                 GLPK.glp_set_col_bnds(lp, i+1, GLPKConstants.GLP_DB, 0, 1);
            }
                     
            // Allocate memory
            ind = GLPK.new_intArray(programs.size()+1);
            val = GLPK.new_doubleArray(programs.size()+1);

            // Create rows
            GLPK.glp_add_rows(lp, 1);

            // Set row details
            GLPK.glp_set_row_name(lp, 1, "c1");
            GLPK.glp_set_row_bnds(lp, 1, GLPKConstants.GLP_UP, 0, wantedExecTime);
            
            for(int i=0;i<programs.size();i++)
            {
	            GLPK.intArray_setitem(ind, i+1, i+1);
	            GLPK.doubleArray_setitem(val, i+1, programs.get(i).getExecTime());
            }
            GLPK.glp_set_mat_row(lp, 1, programs.size(), ind, val);
         

            // Free memory
            GLPK.delete_intArray(ind);
            GLPK.delete_doubleArray(val);

           
            // Define objective
            GLPK.glp_set_obj_name(lp, "z");
            GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);
            GLPK.glp_set_obj_coef(lp, 0, 0);
            for(int i=0;i<programs.size();i++)
            {
            	GLPK.glp_set_obj_coef(lp, i+1, programs.get(i).getExecTime());
            }
          
            // Write model to file
            // GLPK.glp_write_lp(lp, null, "lp.lp");
            iocp = new glp_iocp();
            GLPK.glp_init_iocp(iocp);
            GLPK.glp_term_out(GLPKConstants.GLP_OFF);
            iocp.setPresolve(GLPKConstants.GLP_ON);
            ret = GLPK.glp_intopt(lp, iocp);
          
            int n = GLPK.glp_get_num_cols(lp);
            for (int i = 0; i < n; i++) 
            {
            	wcet = new WCET(programs.get(i).getProgramName(),programs.get(i).getExecTime(),programs.get(i).getLocation());
    			wcet.setNumberOfExec((int)GLPK.glp_mip_col_val(lp, i+1));
    			combinationOfPrograms.add(wcet);	
    		}
    		
            GLPK.glp_delete_prob(lp);
        } catch (GlpkException ex) {
            ex.printStackTrace();
        }
        return combinationOfPrograms;
	}*/
	
	public List<WCET> findProgramCombinationI(double wantedExecTime) 
	{ 
        glp_prob lp;
        glp_iocp iocp;
        SWIGTYPE_p_int ind;
        SWIGTYPE_p_double val;
        int ret;

        List<WCET> combinationOfPrograms = new ArrayList<WCET>();
        WCET wcet;
              
		try {
            // Create problem
            lp = GLPK.glp_create_prob();
            GLPK.glp_set_prob_name(lp, "findWCET");
            GLPK.glp_add_cols(lp, programs.size()*2);
            
            // Create variables and there lower and/or upper bound
            for(int i=0;i<programs.size();i++)
            {
            	GLPK.glp_set_col_name(lp, i+1, "x"+(i+1));
            	GLPK.glp_set_col_kind(lp, i+1, GLPKConstants.GLP_IV);
            	GLPK.glp_set_col_bnds(lp, i+1, GLPKConstants.GLP_LO, 0, 0);
            }
            for(int i=0;i<programs.size();i++)
            {
	            GLPK.glp_set_col_name(lp, programs.size()+i+1, "y"+(i+1));
	            GLPK.glp_set_col_kind(lp, programs.size()+i+1, GLPKConstants.GLP_IV);
	            GLPK.glp_set_col_bnds(lp, programs.size()+i+1, GLPKConstants.GLP_DB, 0, 1);
            }           
                     
            // Allocate memory
            ind = GLPK.new_intArray(programs.size()*2+1);
            val = GLPK.new_doubleArray(programs.size()*2+1);

            // Create rows
            GLPK.glp_add_rows(lp, programs.size()+1);

            // Set row 1 details = summation of the benchmark programs must be fewer than the wanted execution time
            GLPK.glp_set_row_name(lp, 1, "c1");
            GLPK.glp_set_row_bnds(lp, 1, GLPKConstants.GLP_UP, 0, wantedExecTime);
            
            for(int i=0;i<programs.size();i++)
            {
	            GLPK.intArray_setitem(ind, i+1, i+1);
	            GLPK.doubleArray_setitem(val, i+1, programs.get(i).getExecTime());
	            GLPK.intArray_setitem(ind, i+1+programs.size(), i+1+programs.size());
	            GLPK.doubleArray_setitem(val, i+1+programs.size(), (programs.get(i).getNumberOfExec()-1)*programs.get(i).getExecTime());
            }
            // To force that  if x>0; y=1, else; y=0. optimize function: C = k*y + e*x.
            // x <= Integer.Max*y ==> Integer.Min*y + x <= 0
            GLPK.glp_set_mat_row(lp, 1, programs.size()*2, ind, val);
           
            for(int i=0;i<programs.size();i++)
            {
            	GLPK.glp_set_row_name(lp, i+2, "c"+(i+2));
                GLPK.glp_set_row_bnds(lp, i+2, GLPKConstants.GLP_DB, Integer.MIN_VALUE+1, 0);
                
                GLPK.intArray_setitem(ind, 1, i+1);
                GLPK.doubleArray_setitem(val, 1, 1);
                
                GLPK.intArray_setitem(ind, 2, i+1+programs.size());
                GLPK.doubleArray_setitem(val, 2, Integer.MIN_VALUE);
                
                GLPK.glp_set_mat_row(lp, i+2, 2, ind, val);
            }
            // Free memory
            GLPK.delete_intArray(ind);
            GLPK.delete_doubleArray(val);

            // Define objective
            GLPK.glp_set_obj_name(lp, "z");
            GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);
            GLPK.glp_set_obj_coef(lp, 0, 0);
            
            for(int i=0;i<programs.size();i++)
            {
            	GLPK.glp_set_obj_coef(lp, i+1, programs.get(i).getExecTime());
            	GLPK.glp_set_obj_coef(lp, i+1+programs.size(), programs.get(i).getExecTime()*(programs.get(i).getNumberOfExec()-1));
            }
           // GLPK.glp_set_obj_coef(lp, programs.size()+1, cacheFlushTime);

            // Write model to file
            //GLPK.glp_write_lp(lp, null, "lp.lp");
            iocp = new glp_iocp();
            GLPK.glp_init_iocp(iocp);

            iocp.setPresolve(GLPKConstants.GLP_ON);
            GLPK.glp_term_out(GLPKConstants.GLP_OFF);
            ret = GLPK.glp_intopt(lp, iocp);
            
            for (int i = 0; i < programs.size(); i++) 
            {
            	wcet = new WCET(programs.get(i).getProgramName(),programs.get(i).getExecTime(),programs.get(i).getLocation());
    			wcet.setNumberOfExec((int)GLPK.glp_mip_col_val(lp, i+1)+(programs.get(i).getNumberOfExec()-1)*(int)GLPK.glp_mip_col_val(lp, i+1+programs.size()));
    			combinationOfPrograms.add(wcet);
    			//if(programs.get(i).getProgramName().equals("filterbank"))
    				//System.out.println((int)GLPK.glp_mip_col_val(lp, i+1)+" "+(programs.get(i).getNumberOfExec()-1)*(int)GLPK.glp_mip_col_val(lp, i+1+programs.size()));
    				
    		}
            GLPK.glp_delete_prob(lp);
            
        } catch (GlpkException ex) {
            ex.printStackTrace();
        }
        return combinationOfPrograms;
    }
	
	  public void write_mip_solution(glp_prob lp)
	  {
	    int i;
	    int n;
	    String name;
	    double val;
	    
	    name = GLPK.glp_get_obj_name(lp);
	    val  = GLPK.glp_mip_obj_val(lp);
	    System.out.print(name);
	    System.out.print(" = ");
	    System.out.println(val);
	    n = GLPK.glp_get_num_cols(lp);
	    for(i=1; i <= n; i++)
	    {
	      name = GLPK.glp_get_col_name(lp, i);
	      val  = GLPK.glp_mip_col_val(lp, i);
	      System.out.print(name);
	      System.out.print(" = ");
	      System.out.println(val);
	    }
	  }
}
