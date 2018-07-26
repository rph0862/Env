/*
 *  emxProjectMember.java
 *
 * Copyright (c) 1992-2017 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.7.2.2 Thu Dec  4 07:56:10 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.7.2.1 Thu Dec  4 01:55:00 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.7 Wed Oct 22 15:49:55 2008 przemek Experimental przemek $
 */
import matrix.db.*;
import java.util.*;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import matrix.db.Context;
import matrix.util.StringList;
import com.matrixone.apps.domain.util.EnoviaResourceBundle; 
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.util.MatrixException;

/**
 * The <code>emxProjectMember</code> class represents the Project Member JPO
 * functionality for the PMC type.
 *
 * @version PMC 10-6 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxProjectMember_mxJPO extends emxProjectMemberBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public emxProjectMember_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }

	/**
  * Method controls edit access for Access column on Member page
  * If user is from CIVIL dept and not owner of project then user should not have edit access
  * if CIVIL + Owner then only can edit 
  * For non-civil users it should work as previous 
  * 
  */
   public MapList getMembersDynamicColumns(Context context, String[] args) throws Exception{
	   MapList returnMapList = new MapList();
	   try{
		   Map inputMap = JPO.unpackArgs(args);
		   String sContextsUser = context.getUser();
		   Map requestMap = (Map) inputMap.get("requestMap");
		   String sProjectId = (String) requestMap.get("parentOID");
		   DomainObject domProject = DomainObject.newInstance(context,sProjectId);
		   String strAccess = domProject.getInfo(context,"current.access[changeowner]");
		   String sOwner = domProject.getInfo(context, DomainConstants.SELECT_OWNER);
		   returnMapList = super.getMembersDynamicColumns(context, args);
		   MSILAccessRights_mxJPO msilAccess=new MSILAccessRights_mxJPO(context,args);
            boolean isNonCivilCIVIL  = msilAccess.hasAccessForOOTBCommand(context, "WMSCIVIL.DepartmentDashboard.ENAccess");
            if(!isNonCivilCIVIL && !sOwner.equalsIgnoreCase(sContextsUser)){
            	  Map accessCol = (Map)returnMapList.get(2);
            	  ((Map)(accessCol.get("settings"))).put("Editable", "false");
             }
			 //added to hide organization column - START
			 Map organizationCol = (Map)returnMapList.get(1);
			 ((Map)(organizationCol.get("settings"))).put("Access Expression", "false");
			 //END
			 
	    //added check for PIC should not edit role column
	      MQLCommand mql=new MQLCommand();
	      mql.executeCommand(context,"PRINT BUS "+sProjectId+" select from["+DomainRelationship.RELATIONSHIP_MEMBER+"|attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"]=='SectionManager'].to.name dump");
              String strResult =  mql.getResult();
	      Map rolesCol = (Map)returnMapList.get(3);
	     if(!sOwner.equalsIgnoreCase(sContextsUser) && !strResult.trim().equalsIgnoreCase(sContextsUser) )   {
            	   ((Map)(rolesCol.get("settings"))).put("Editable", "false");
		   }
		     // Add Validation methods 
             Map accessCol = (Map)returnMapList.get(2);
            ((Map)(accessCol.get("settings"))).put("Validate", "validateSMnPICCellValue");  //access column
             accessCol = (Map)returnMapList.get(3);
             ((Map)(accessCol.get("settings"))).put("Validate", "validateProjectRole");  //Project Role column

			//Modified for B3Action -start
			boolean isCivilCIVIL  = msilAccess.isPersonFromCivil(context, args);
			if(isCivilCIVIL){
				Map mapColumn = new HashMap();
				mapColumn.put("name", "LastLoginedTime");
				mapColumn.put("label", "WMS.table.column.lastloggedinmember");
				mapColumn.put("expression_businessobject", "attribute[Last Login Date]");
				Map mapSettings = new HashMap();
				mapSettings.put("Editable","false");
				mapSettings.put("Registered Suite","WMS");
				mapSettings.put("Field Type","attribute");
				mapSettings.put("Column Type","program");
				mapSettings.put("function","getLastLoginOfUser");
				mapSettings.put("program","emxProjectMember");
				mapColumn.put("settings", mapSettings);
				returnMapList.add(mapColumn); 
			}
			//Modified for B3Action -end
	   }catch(Exception e){
		   e.printStackTrace();
	   }
	   return returnMapList;
   }
 
	
public boolean isCIVILProjectInCharge(Context context,String[] args){ 

	       try{
	       Map inputMap = JPO.unpackArgs(args);
		   String sContextsUser = context.getUser();
		   String sProjectId = (String) inputMap.get("objectId");
		    DomainObject domProject = DomainObject.newInstance(context,sProjectId);
		     String sOwner = domProject.getInfo(context, DomainConstants.SELECT_OWNER);
		       if(sOwner.equalsIgnoreCase(sContextsUser)){
		       		    return true;						    
		       } else  {
		       	 MQLCommand mql=new MQLCommand();
				 mql.executeCommand(context,"PRINT BUS "+sProjectId+" select from["+DomainRelationship.RELATIONSHIP_MEMBER+"|attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"]=='ProjectInCharge'].to.name dump");
                          String strResult =  mql.getResult();
				if(strResult.trim().equalsIgnoreCase(sContextsUser)) 
	 			return true;
		       
		       
		       }

		   }   catch(Exception e){
		   
		   
		  
		   }





 return false;	
 }
public Vector getLastLoginOfUser(Context context,String[] args) throws Exception{
	Vector vColum = new Vector();
	 DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss a") ;
	 DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MMM-dd-yyyy hh:mm:ss a");
	try {
		Map mInputMap =  JPO.unpackArgs(args);
		MapList objectList = (MapList)mInputMap.get("objectList");
	    Iterator itr  = objectList.iterator();
		String strName=DomainConstants.EMPTY_STRING;
		String strPersonId=DomainConstants.EMPTY_STRING; 
		String strLastLoginDate=DomainConstants.EMPTY_STRING;
		DomainObject domPerson=DomainObject.newInstance(context);
		while(itr.hasNext()) {
			Map m = (Map) itr.next();
		    strName=(String) m.get("username");
			strPersonId = PersonUtil.getPersonObjectID(context, strName);
			domPerson.setId(strPersonId);
			strLastLoginDate = domPerson.getAttributeValue(context, "Last Login Date");
			LocalDateTime ldt = dtf.parseLocalDateTime(strLastLoginDate);
			vColum.add(dtfOut.print(ldt));
		}
	 		
	}catch(Exception e) {
		e.printStackTrace();
	}
 	
	return vColum;
}

    /**
     * getCompName - This method shows the company person members belong to.
     * Used for PMCProjectMemberSummary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     * @return Vector containing the organisation value as String
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
	 @com.matrixone.apps.framework.ui.ProgramCallable
    public Vector getCompName(Context context, String[] args)
    throws Exception
    {
        Vector vectCompany = new Vector();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        try
        {
            com.matrixone.apps.common.Person person =
                (com.matrixone.apps.common.Person) DomainObject.newInstance(context,
                        DomainConstants.TYPE_PERSON);
            Map objectMap = null;
            Iterator objectListIterator = objectList.iterator();

            while (objectListIterator.hasNext())
            {
                StringBuffer equivalentLink = new StringBuffer();
                objectMap = (Map) objectListIterator.next();
                //String organization = (String) objectMap.get(person.SELECT_COMPANY_NAME);
				
				//Code added to get organization - Bug 11165 -START
				String strUsername=(String)objectMap.get("username");
				String organization =DomainConstants.EMPTY_STRING;
				if(UIUtil.isNotNullAndNotEmpty(strUsername))
				{
				String strPersonObjId	= PersonUtil.getPersonObjectID(context, strUsername);
				DomainObject domPerson=new DomainObject(strPersonObjId);
				organization = (String)domPerson.getInfo(context, "to[Employee].from.name");
				}
				if (UIUtil.isNullOrEmpty(strUsername))
                {
                    organization = (String)objectMap.get("org");
                }
				//Custom code ends here
                /*if ((organization == null) || "null".equals(organization))
                {
                    organization = "";
                }*/
                vectCompany.add(organization);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return vectCompany;
        }
    }

}
