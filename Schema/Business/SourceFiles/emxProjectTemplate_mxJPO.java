//
// $Id: emxProjectTemplate.java.rca 1.6 Wed Oct 22 16:21:23 2008 przemek Experimental przemek $ 
//
// emxProjectTemplate.java
//
// Copyright (c) 2002-2015 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import com.matrixone.apps.program.ProjectTemplate;

import matrix.db.*;

/**
 * The <code>emxProjectTemplate</code> class represents the Project Template JPO
 * functionality for the AEF type.
 *
 * @version AEF 10.0.SP4 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxProjectTemplate_mxJPO extends emxProjectTemplateBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     * @grade 0
     */
    public emxProjectTemplate_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
    
    /**
     *  Method which moves Project Template to Inactive State on Project Template Creation
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @Owner -Ravi MSIL 18X
     */
   
     public int triggerPromoteToInactiveState(Context context,String[] args) throws Exception
    {
       try {	
    	      String strPTemplate=args[0];
    	       ProjectTemplate pt=new ProjectTemplate(strPTemplate);
    	       pt.promote(context);
     
          }catch(Exception e) {
    	  
    	  e.printStackTrace();
      }
      return 0;
    }
    
}
