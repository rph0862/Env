/*
 ${CLASSNAME}.java
 * This JPO Contains Code to Transfer current and Future Pending Task to the new person if a person left the Organization.
 * 24-Feb-2016   |   Ajit    |  Set cron job on thursday to send mail regading Delayed Task Person Information List to Prakhar and Puran singh
 * 28/04/2016    |   Ajit    |  To transfer Current And Future Task And Route of the old person to new person
 * 01-Jun-2016   |   Dheeraj    |  To transfer Current And Future Task And Route of the old person to new person
 * 29-Jun-2018   |   Vinit   | Slip days calculation to be done based on estimated start date and percent completion
*/

import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.Locale;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.User;
import matrix.util.StringList;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import java.io.BufferedWriter;
import java.io.FileWriter;
import com.matrixone.apps.domain.util.FrameworkException;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;

public class MSILUtility_mxJPO extends com.matrixone.apps.program.Task implements MSILConstants_mxJPO
//public class ${CLASSNAME} extends ${CLASS:emxDomainObject}
{

    public MSILUtility_mxJPO (Context context, String[] args) throws Exception

    {
        //super(context, args);       
        
    }
/**
       * This method is executed if a specific method is not specified.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @returns int
       * @throws Exception if the operation fails
       * @since PMC 10.5.1.2
       */
      public int mxMain(Context context, String[] args)
          throws Exception
      {
         // Please don't remove below line and Dont set any cron in this JPO as it is running from cron will be called if it will call mxMain() method 
         System.out.println("\n calling mxMain() !!!");          
         delayedTaskList(context,args);
         return 0;
      }
    
    /**
     * Method to Transfer Task from Old person to New Person.Plese exec by logging in "Test Everything" 
     * 
     */
    public void changeAssignee(Context context,String [] args) throws Exception
    {       
        //exec prog MSILUtility -method changeAssignee OldPersonName NewPersonName;
        //exec prog MSILUtility -method changeAssignee 207764 244058;   
        try
        {
            System.out.println("\n\n Calling changeAssignee() !!!");
            if(args.length==2)
            {
                String strOldPerson = args[0];
                String strNewPerson = args[1];
                System.out.println("\n strOldPerson !!! "+strOldPerson);
                System.out.println("\n strNewPerson !!! "+strNewPerson);
                String strOlePersonId ="";
                DomainObject domOldPerson = null;
                StringList slPersonSelect = new StringList();
                slPersonSelect.addElement(DomainConstants.SELECT_ID);               
                StringList slObjectSelects = new StringList();
                slObjectSelects.add(DomainConstants.SELECT_ID); 
                slObjectSelects.add(DomainConstants.SELECT_NAME);
                slObjectSelects.add(DomainConstants.SELECT_CURRENT);
                slObjectSelects.add("attribute[Scheduled Completion Date]");
                                
                MapList mlOldPersonList =  DomainObject.findObjects(context,
                    DomainConstants.TYPE_PERSON,
                    strOldPerson.trim(),
                    "*",
                    "*",
                    "*",
                    "",
                    false,
                    slPersonSelect);
                    //System.out.println("\n mlOldPersonList !!! "+mlOldPersonList);
                    if( null != mlOldPersonList && mlOldPersonList.size()>0)
                    {
                    strOlePersonId = (String)(((Map)mlOldPersonList.get(0)).get(DomainConstants.SELECT_ID));
                    }               
                    
                    if(!"".equals(strOlePersonId) && !"null".equals(strOlePersonId)&& null!=strOlePersonId){
                          domOldPerson = DomainObject.newInstance(context,strOlePersonId);
                          if( null !=domOldPerson){
                             
                              // To Get All Pending Tasks of the old user.
                              MapList mlPendingTasks = domOldPerson.getRelatedObjects(context,"Project Task","Inbox Task",slObjectSelects,null,true,true,(short)1,"current !='Complete'",null);
                              //System.out.println("\n mlPendingTasks !!! "+mlPendingTasks.size());
                              if( null != mlPendingTasks && mlPendingTasks.size()>0){
                                  for(int task=0,mlPendingTasksSize=mlPendingTasks.size();task<mlPendingTasksSize;task++){
                                      Map taskMap = (Map)mlPendingTasks.get(task);
                                      String strTaskId = (String)taskMap.get(DomainConstants.SELECT_ID); 
                                      DomainObject domInboxTaskObj = DomainObject.newInstance(context,strTaskId);                 
                                      domInboxTaskObj.setAttributeValue(context, "Allow Delegation", "TRUE");
                                      String aMethodArgs[] = new String[3];
                                      aMethodArgs[0] =strTaskId ;
                                      aMethodArgs[1] =strOldPerson ;
                                      aMethodArgs[2] =strNewPerson ;                                     
                                      //call method taskTransfer
                                      taskTransfer(context,aMethodArgs);
                                                            
                                
                                  }
                              }
                              
                             // TO Transfer Future Tasks of the Routes which are in Define State -- Start
                             StringList slSelects = new StringList();
                             slSelects.addElement(SELECT_NAME);
                             slSelects.addElement(SELECT_ID);
                             slSelects.addElement(SELECT_CURRENT);          
                             slSelects.addElement("from[Project Route].to.name");
                             slSelects.addElement("from[Route Node].to.name");          
                             //String sWhereExp = "current ~~ 'Define' && ( (from[Project Route].to.name ~~ '"+strOldPerson+"') || (from[Route Node].to.name ~~ '"+strOldPerson+"') )";
                             String sWhereExp = "(current ~~ 'Define' || current ~~ 'In Process') && (from[Route Node].to.name ~~ '"+strOldPerson+"')";
                             System.out.println("\n sWhereExp !!!"+sWhereExp);
                             MapList mlRoute = DomainObject.findObjects(context,
                                "Route",
                                "*",
                                sWhereExp,
                                slSelects);
                            System.out.println("\n No Of Route !!!"+mlRoute.size());
                            //System.out.println("\n mlRoute !!!"+mlRoute);
                            if(null != mlRoute && mlRoute.size()>0){           
                                for(int r=0;r<mlRoute.size();r++){
                                Map mapRouteInfo = (Map)mlRoute.get(r);
                                String sRouteId =(String) mapRouteInfo.get(SELECT_ID);
                                //Method to Transfer Route details                          
                                updateRouteDetails(context,sRouteId,strOldPerson,strNewPerson);
                                 }
                            }
                             
                            // TO Transfer Future Tasks of the Routes which are in Define State -- End       
                            
                        }
                    }
                
            }
        }
        catch (Exception ex)
        {

        }
    }
    
    /**
     * Method to Transfer Task from Old person to New Person.Plese exec by logging in "Test Everything" 
     * 
     */

    public void taskTransfer(Context context,String [] args) throws Exception
        {
            
        //exec prog MSILUtility -method taskTransfer InboxTaskId OldPersonName NewPersonName;
        //exec prog MSILUtility -method taskTransfer 38076.31588.2321.2903 206008 190780;  
            
        try
        {
        System.out.println("\n\n Calling taskTransfer() !!!");
        if(args.length==3){
        String sInboxTaskId = args[0];
        String strOldPerson = args[1];
        String strNewPerson = args[2];
        DomainObject domInboxTask = null;
        StringList slObjectSelects = new StringList();
        slObjectSelects.add(DomainConstants.SELECT_ID); 
        slObjectSelects.add(DomainConstants.SELECT_NAME);
        slObjectSelects.add(DomainConstants.SELECT_CURRENT);
        ContextUtil.startTransaction(context, true);
        ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
        if(!"".equals(sInboxTaskId)&& !"null".equals(sInboxTaskId)&& null!=sInboxTaskId){
            domInboxTask = DomainObject.newInstance(context,sInboxTaskId);
            String sState = (String)domInboxTask.getInfo(context,"current");
            //String sTaskTitle = (String)domInboxTask.getInfo(context,"attribute[Title]");
            System.out.println("\n sState !!! " +sState);
            //System.out.println("\n sTaskTitle !!! "+sTaskTitle);
            if(!("Complete".equals(sState))){

                if(!"".equals(sInboxTaskId)&& !"null".equals(sInboxTaskId)&& null!= sInboxTaskId){
                    domInboxTask = DomainObject.newInstance(context,sInboxTaskId);
                    String sPersonName = (String)domInboxTask.getInfo(context,"from[Project Task].to.name");
                    String sRelid = (String)domInboxTask.getInfo(context,"from[Project Task].id");
                    if((!"".equals(sPersonName)&& !"null".equals(sPersonName)&& null!=sPersonName) && strOldPerson.equals(sPersonName)){

                        // To Modify the old person connection to new Person
                        String strMQL = "mod connection "+sRelid+" to Person "+strNewPerson+" -";
                        MqlUtil.mqlCommand(context,strMQL);

                        // check if the owner and originator of the "Inbox Task" is old Person then change it to new Person -- Start

                        String sInboxTaskOriginator = (String)domInboxTask.getAttributeValue(context,"originator");
                        if((!"".equals(sInboxTaskOriginator)&& !"null".equals(sInboxTaskOriginator)&& null!=sInboxTaskOriginator) && strOldPerson.equals(sInboxTaskOriginator)){
                            domInboxTask.setAttributeValue(context,"originator",strNewPerson);

                        }
                        User userInboxIask = (User)domInboxTask.getOwner(context);
                        String sInboxTaskOwner = userInboxIask.getName();
                        if((!"".equals(sInboxTaskOwner)&& !"null".equals(sInboxTaskOwner)&& null!=sInboxTaskOwner) && strOldPerson.equals(sInboxTaskOwner)){
                            ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                            domInboxTask.setOwner(context,strNewPerson);    
                            ContextUtil.popContext(context);

                        }

                        // check if the owner and originator of the "Inbox Task" is old Person then change it to new Person -- End
                    }
                    // To get Associated Route Id to Revoke access from old Person and Grant access to new Person -- Start

                    String sRouteId = domInboxTask.getInfo(context,"from[Route Task].to.id");
                    if(!"".equals(sRouteId)&& !"null".equals(sRouteId)&& null!=sRouteId){
                    updateRouteDetails(context,sRouteId,strOldPerson,strNewPerson);
                    }

                    
                }
                System.out.println("\n Successfully transfered !!!");
                ContextUtil.popContext( context);
                // If Sucessfully executed  then Commited to DB
                ContextUtil.commitTransaction(context);

            }else{
                System.out.println("\n Not A Appropriate Inbox Task to be Transfered !!!");
            }
        }
    }
    
}catch (Exception e) {
    ContextUtil.abortTransaction(context);
    System.out.println("Transaction aborted as Exception Due to :: "+e.getMessage());
}
}
    
    /**
     * Method to Transfer  Route Details from Old person to New Person.
     * 
     */

 public void updateRouteDetails(Context context,String sRouteId,String strOldPerson,String strNewPerson) throws Exception
    {       
        //exec prog MSILUtility -method changeAssignee OldPersonName NewPersonName;
        //exec prog MSILUtility -method changeAssignee 207764 244058;   
        try
        {       
         //System.out.println("\n Calling to updateRouteDetails() !!!");                    
         StringList slObjectSelects = new StringList();
         slObjectSelects.add(DomainConstants.SELECT_ID);    
         slObjectSelects.add(DomainConstants.SELECT_NAME);
         slObjectSelects.add(DomainConstants.SELECT_CURRENT);          
                
        DomainObject domRoute = null;
        if(!"".equals(sRouteId)&& !"null".equals(sRouteId)&& null!=sRouteId){

            domRoute = DomainObject.newInstance(context,sRouteId);

        }
        if(null != domRoute ){

            // To Change Owner and Originator from Old Person to New person if anything Exist -- Start

            /*
            String sRouteOriginator = (String)domRoute.getAttributeValue(context,"originator");
            if((!"".equals(sRouteOriginator)&& !"null".equals(sRouteOriginator)&& null!=sRouteOriginator) && strOldPerson.equals(sRouteOriginator)){
                domRoute.setAttributeValue(context,"originator",strNewPerson);
            }

            User userRoute = (User)domRoute.getOwner(context);
            String sRouteOwner = userRoute.getName();
            if((!"".equals(sRouteOwner)&& !"null".equals(sRouteOwner)&& null!=sRouteOwner) && strOldPerson.equals(sRouteOwner)){
                ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                domRoute.setOwner(context,strNewPerson);
                ContextUtil.popContext(context);
            }
             */
            // To Owner and Originator from Old Person to New person if anything Exist -- Start

            // To Grant and Revoke from Old Person to New person if anything Exist -- Start

            StringList slGranteeList = domRoute.getGrantees(context);

            if(null != slGranteeList && slGranteeList.contains(strOldPerson)){
                ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                //String strRevokeCommand = "mod bus "+sRouteId+" revoke grantee "+strOldPerson;
                //MqlUtil.mqlCommand(context,strRevokeCommand);
                String strGrantCommand = "mod bus "+sRouteId+" grant "+strNewPerson+" access all";
                MqlUtil.mqlCommand(context,strGrantCommand);    
                ContextUtil.popContext( context);
            } 

            // To Grant and Revoke from Old Person to New person if anything Exist -- End

            // To Modify connection of Incomplete Route Object from Old Person to New person if anything Exist -- Start

            /*// To Modify the "Project Route" connected person if it is having old person -- Start
            String sProjectRouteName =(String)domRoute.getInfo(context,"from[Project Route].to.name");
            if((!"".equals(sProjectRouteName)&& !"null".equals(sProjectRouteName)&& null!=sProjectRouteName) && strOldPerson.equals(sProjectRouteName)){
                String sProjectRouteId =(String)domRoute.getInfo(context,"from[Project Route].id");
                ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                String strProjectRouteMQL = "mod connection "+sProjectRouteId+" to Person "+strNewPerson+" -";
                MqlUtil.mqlCommand(context,strProjectRouteMQL);
                ContextUtil.popContext( context);
            }
            // To Modify the "Project Route" connected person if it is having old person -- End
            */

            StringList slRelSelect = new StringList();
            slRelSelect.addElement("id[connection]");

            String relWhere = "attribute[Actual Completion Date]==''";
            MapList mlRoutePerson = domRoute.getRelatedObjects(context,"Route Node",DomainConstants.TYPE_PERSON,slObjectSelects,slRelSelect,true,true,(short)1,null,relWhere);
            if(null != mlRoutePerson && mlRoutePerson.size()>0){
                for(int route=0;route<mlRoutePerson.size();route++){
                    Map mapRoutePerson = (Map) mlRoutePerson.get(route);
                    String sRoutePersonName = (String)mapRoutePerson.get(DomainConstants.SELECT_NAME);
                    if((!"".equals(sRoutePersonName)&& !"null".equals(sRoutePersonName)&& null!=sRoutePersonName) && strOldPerson.equals(sRoutePersonName)){
                        String sRoutePersonRelId = (String) mapRoutePerson.get("id[connection]");
                        ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                        String strROutePersonMQL = "mod connection "+sRoutePersonRelId+" to Person "+strNewPerson+" -";                                             
                        MqlUtil.mqlCommand(context,strROutePersonMQL);                                              
                        ContextUtil.popContext( context);
                    }
                }
            }
            // To Modify connection of Incomplete Route Object from Old Person to New person if anything Exist -- End

        }
        

        }catch (Exception e) {
            ContextUtil.abortTransaction(context);
            System.out.println("Transaction aborted as Exception Due to :: "+e.getMessage());
        }
    }

 // 24-Feb-2016   |   Ajit    |  Set cron job on thursday to send mail regading Delayed Task Person Information List to Prakhar and Puran singh -- Start
 /**
 * This method is cron running on thursday to send mail regading Delayed Task Person Information List to Prakhar and Puran singh
 * @return void.
 * @throws Exception if operation fails.
 * @author Ajit
 */

    public void delayedTaskList (Context context, String args[]) throws Exception
    {
        FileWriter detailsLog = new FileWriter("/////appv62013x//Scripts//logs//delayedTaskList.log");          
        detailsLog.write("\n.. MSILUtility : delayedTaskList() -- Start >>."+new Date());detailsLog.flush();
        int noOfRowToBeWrite=0;
        String strTransPath =   context.createWorkspace();         
        File templateFile = new File(strTransPath);
        String strFileName="PersonInfo.xls";
        String strFilePath = strTransPath+"/"+strFileName;      
        FileWriter fw = new FileWriter(strFilePath);
        BufferedWriter outPersonInfo = new BufferedWriter(fw);
		// Added by Vinit - Start 29-Jun-2018
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		// Added by Vinit - End 29-Jun-2018
          try{   

                 DomainConstants.MULTI_VALUE_LIST.add("from[Assigned Tasks].to.name");
                 DomainConstants.MULTI_VALUE_LIST.add("from[Assigned Tasks].to.id");
                 DomainConstants.MULTI_VALUE_LIST.add("from[Assigned Tasks].to.attribute[Task Estimated Finish Date]");
                 DomainConstants.MULTI_VALUE_LIST.add("from[Assigned Tasks].to.current");
                 DomainConstants.MULTI_VALUE_LIST.add("from[Assigned Tasks].to.to[Project Access Key].from.from[Project Access List].to.name");
                 DomainConstants.MULTI_VALUE_LIST.add("from[Assigned Tasks].to.to[Project Access Key].from.from[Project Access List].to.current");                 
                 
                 SimpleDateFormat sdf      = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
                 Date strCurrentDate = new Date();
                 
                 DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                 Date date = null;
                 
                 StringList objectSelectsList = new StringList();
                 objectSelectsList.addElement(DomainObject.SELECT_ID);
                 objectSelectsList.addElement(DomainObject.SELECT_TYPE);
                 objectSelectsList.addElement(DomainObject.SELECT_NAME);

                 StringList objectList = new StringList();
                 objectList.addElement(DomainObject.SELECT_ID);
                 objectList.addElement(DomainObject.SELECT_NAME);
                 objectList.addElement("attribute[Last Login Date]");   
                 //objectList.add("to[Member|(from.type==Department)].from.id");
                 objectList.add("to[Member|(from.type==Department)].from.name");
                 //objectList.add("to[Member|(from.type==Department)].from.type");
                 objectList.add("from[Assigned Tasks].to.name");
                 objectList.add("from[Assigned Tasks].to.id");
                 //objectList.add("from[Assigned Tasks|(to.attribute[Task Estimated Finish Date]<"+strCurrentDate+")].to.name");
                 objectList.add("from[Assigned Tasks].to.attribute[Task Estimated Finish Date]");
                 objectList.add("from[Assigned Tasks].to.current");
                 objectList.add("from[Assigned Tasks].to.to[Project Access Key].from.from[Project Access List].to.name");
                 objectList.add("from[Subtask]");
                 // 6 Aug 2015 - Start
                 objectList.add("from[Assigned Tasks].to.to[Project Access Key].from.from[Project Access List].to.current");
                 StringList slProjectCurrent = new StringList();
                 // 6 Aug 2015 - End
                 String strWhereclause = "current==Active && from[Assigned Tasks]==True";
                 MapList mlPersonList = DomainObject.findObjects(context, DomainConstants.TYPE_PERSON, "*", strWhereclause, objectList);
                 String sDept = "";
                 String sAssignee = "";
                 StringList slProject = new StringList();
                 StringList slTask = new StringList();
                 StringList slTaskId = new StringList();
                 StringList slState  = new StringList();
                 DomainObject domTaskId = null;
                 String sLastLogin = "";
                 //outPersonInfo.write("Department\tAssignee\tProject\tTask\tState\tLast Login");
                 outPersonInfo.write("Department\tAssignee\tProject\tTask\tState\tSlip Days\tLast Login");
                 outPersonInfo.write("\n");
                 int nPersonListSize = 0;
                 if(null != mlPersonList)
                       nPersonListSize = mlPersonList.size();
					   
				// Added by Vinit - Start 29-Jun-2018   
				StringList taskSelects = new StringList(3);
				taskSelects.add(task.SELECT_TASK_ESTIMATED_START_DATE);
				taskSelects.add(task.SELECT_TASK_ESTIMATED_DURATION);
				taskSelects.add(DomainObject.SELECT_PERCENTCOMPLETE);
				// Added by Vinit - End 29-Jun-2018   

                 for(int nPersonCount = 0; nPersonCount < nPersonListSize; nPersonCount++)
                 {     
                       StringList slTaskEstFinishDate = new StringList();
                       Map mpPerson = (Map)mlPersonList.get(nPersonCount);
                       
                       if(mpPerson.get("from[Assigned Tasks].to.attribute[Task Estimated Finish Date]") instanceof String)
                        {
                        String strTaskEstFinishDate = (String) mpPerson.get("from[Assigned Tasks].to.attribute[Task Estimated Finish Date]");
                        slTaskEstFinishDate.add(strTaskEstFinishDate);
                        }else if(mpPerson.get("from[Assigned Tasks].to.attribute[Task Estimated Finish Date]") instanceof StringList)
                        {
                            slTaskEstFinishDate = (StringList) mpPerson.get("from[Assigned Tasks].to.attribute[Task Estimated Finish Date]");
                        }
                       //date = df.parse(sTaskEstFinishDate);
                       if(null!=slTaskEstFinishDate)
                       {
                           for(int l=0;l<slTaskEstFinishDate.size();l++)
                           {
                               String sTaskEstFinishDate = (String) slTaskEstFinishDate.get(l);
                               //detailsLog.write("\n sTaskEstFinishDate >> "+sTaskEstFinishDate);detailsLog.flush();
                               date = eMatrixDateFormat.getJavaDate(sTaskEstFinishDate);
                              
                               if(strCurrentDate.compareTo(date)>0)
                               {
                                   sDept = (String) mpPerson.get("to[Member].from.name");
                                   detailsLog.write("\n sDept >> "+sDept);detailsLog.flush();
                                   sAssignee = (String) mpPerson.get(DomainObject.SELECT_NAME);
                                   detailsLog.write("\n sAssignee >> "+sAssignee);detailsLog.flush();
                                   if(null==sDept)
                                   {
                                    sDept = "";
                                   }
                                   if(null==sAssignee)
                                   {
                                    sAssignee = "";
                                   }
                                   slProject = (StringList) mpPerson.get("from[Assigned Tasks].to.to[Project Access Key].from.from[Project Access List].to.name");
                                   // 6 Aug 2015 - Start
                                   slProjectCurrent = (StringList) mpPerson.get("from[Assigned Tasks].to.to[Project Access Key].from.from[Project Access List].to.current");
                                   // 6 Aug 2015 - End
                                   slTask = (StringList) mpPerson.get("from[Assigned Tasks].to.name");
                                   slTaskId = (StringList) mpPerson.get("from[Assigned Tasks].to.id");
                                   slState = (StringList) mpPerson.get("from[Assigned Tasks].to.current");
                                   sLastLogin = (String) mpPerson.get("attribute[Last Login Date]");
                                   if(null==sLastLogin)
                                   {
                                    sLastLogin = "";
                                   }
                                   //detailsLog.write("\n slProject.size() >> "+slProject.size());detailsLog.flush();
                                    if(slProject.size() >0)
                                    {
                                        // 6 Aug 2015 - Start
                                        String sProjectState = (String) slProjectCurrent.get(l);
                                        //detailsLog.write("\n sProjectState >> "+sProjectState);detailsLog.flush();
                                        if(!"Cancel".equalsIgnoreCase(sProjectState) && !"Hold".equalsIgnoreCase(sProjectState))
                                        {
                                        // 6 Aug 2015 - End
                                        String sProject = (String) slProject.get(l);
                                        String sTask = (String) slTask.get(l);
                                        String sTaskId = (String) slTaskId.get(l);
                                        domTaskId = DomainObject.newInstance(context,sTaskId);
										// Added by Vinit - start 29-Jun-2018
                                        //String isTaskLeafLevel = domTaskId.getInfo(context,"from[Subtask]"); //False - leaf level task
										
										Map mpTaskInfo = domTaskId.getInfo(context, taskSelects);
										
										String isTaskLeafLevel       = (String)mpTaskInfo.get("from[Subtask]");
										String strTaskStartDate      = (String)mpTaskInfo.get(task.SELECT_TASK_ESTIMATED_START_DATE);
										String strDuration           = (String)mpTaskInfo.get(task.SELECT_TASK_ESTIMATED_DURATION);
										String strPercentComplete    = (String)mpTaskInfo.get(DomainObject.SELECT_PERCENTCOMPLETE);
										// Added by Vinit - end 29-Jun-2018
                                        //detailsLog.write("sTask.......>>"+sTask);detailsLog.flush();
                                        detailsLog.write("isTaskLeafLevel.......>>"+isTaskLeafLevel);detailsLog.flush();
                                        //detailsLog.write("\n isTaskLeafLevel >> "+isTaskLeafLevel);detailsLog.flush();
                                        String strTaskSlipDays = "";
                                        if(null!=isTaskLeafLevel && !"".equals(isTaskLeafLevel) && isTaskLeafLevel.equals("False"))
                                        {  
											// Added by Vinit - start 29-Jun-2018
											// strTaskSlipDays = calculateSlipDays(context, sTaskId, true, sTaskEstFinishDate);                           
                                            strTaskSlipDays = calculateSlipDays(context, args, sTaskEstFinishDate, strTaskStartDate, strDuration, strPercentComplete);
											// Added by Vinit - end 29-Jun-2018
                                        }else{
											// Added by Vinit - start 29-Jun-2018
											// strTaskSlipDays = calculateSlipDays(context, sTaskId, true, sTaskEstFinishDate);
                                            strTaskSlipDays = calculateSlipDays(context, args, sTaskEstFinishDate, strTaskStartDate, strDuration, strPercentComplete);
											// Added by Vinit - end 29-Jun-2018
                                        }
                                        detailsLog.write("\n strTaskSlipDays >> "+strTaskSlipDays);detailsLog.flush();
                                        
                                        String sState = (String) slState.get(l);
                                        detailsLog.write("\n sState >> "+sState);detailsLog.flush();
                                        if(null!=sState && !sState.equals("Complete"))
                                        {                                           
                                            outPersonInfo.write(sDept+"\t");outPersonInfo.flush();
                                            outPersonInfo.write(sAssignee+"\t");outPersonInfo.flush();
                                            outPersonInfo.write(sProject+"\t");outPersonInfo.flush();
                                            outPersonInfo.write(sTask+"\t");outPersonInfo.flush();
                                            outPersonInfo.write(sState+"\t");outPersonInfo.flush();
                                            outPersonInfo.write(strTaskSlipDays+"\t");outPersonInfo.flush();
                                            outPersonInfo.write(sLastLogin+"\t");outPersonInfo.flush();
                                            outPersonInfo.write("\n");
                                            noOfRowToBeWrite++;
                                            
                                            }
                                        }
                                    }
                                }
                            }
                        }
                 }

                // To send mail to Specific user -- Start           
               
                StringBuffer sbMailSubject = new StringBuffer();
                StringBuffer sbHTMLBody = new StringBuffer();               
                sbMailSubject.append("Enovia:Delayed Task Person Information List");                        
                sbHTMLBody.append("<html><body>");                                         
                sbHTMLBody.append("<span style='font-size:10.0pt;font-family:\"Calibri\",\"sans-serif\";color:navy'><br>");
                sbHTMLBody.append("Dear All,");
                sbHTMLBody.append("<br>");                          
                sbHTMLBody.append("Please Find Attached list of users having delayed task.</span>");                            
                sbHTMLBody.append("<br><br>");              
                sbHTMLBody.append("<span style='font-size:10.0pt;font-family:\"Calibri\",\"sans-serif\";color:navy'>Thanks &amp; Regards");    
                sbHTMLBody.append("<br>");
                sbHTMLBody.append("</span>");               
                sbHTMLBody.append("</body></html>");
                //logFile.write("\n\n "+sbHTMLBody.toString());  logFile.flush();
                
               
               String MAIL_FROM = "PM.Support@maruti.co.in";
               String MAIL_TO = "prakhar.srivastava1@maruti.co.in,;PuranSingh.Rawat@maruti.co.in";
               //String MAIL_TO = "INTELIZIGN_AjitKumar.Balabantaray@maruti.co.in";
               String MAIL_CC = "";
               String MAIL_BCC = "";
               String strSubject = sbMailSubject.toString();
               String strMailBody = sbHTMLBody.toString();                          
                      
                String saArgs [] = new String [0];
               MSILTaskReminderMail_mxJPO msilTaskReminderMail = new MSILTaskReminderMail_mxJPO (context,saArgs);
                boolean isMailSent = msilTaskReminderMail.sendMailHtmlFormat ( MAIL_FROM,  MAIL_TO,  MAIL_CC,  MAIL_BCC,  strSubject,  strMailBody,  strFilePath, strFileName);
                detailsLog.write("\n isMailSent >> "+isMailSent);detailsLog.flush();
                // To send mail to Specific user -- End 
                // To delete the File if mail sent to user 
                if(isMailSent){
                delete(templateFile);
                }
                detailsLog.write("\n\n noOfRowToBeWrite >> "+noOfRowToBeWrite);detailsLog.flush();
                detailsLog.write("\n  MSILUtility : delayedTaskList() -- End >> "+new Date());detailsLog.flush();
          } catch (Exception e) {
                 //throw new FrameworkException(e);
                detailsLog.write("Exception e........>>>>>>"+e);detailsLog.flush();
                e.printStackTrace();  
          }
          finally
          {
            outPersonInfo.close();
            
            
          }

          DomainConstants.MULTI_VALUE_LIST.remove("from[Assigned Tasks].to.name");
          DomainConstants.MULTI_VALUE_LIST.remove("from[Assigned Tasks].to.id");
          DomainConstants.MULTI_VALUE_LIST.remove("from[Assigned Tasks].to.attribute[Task Estimated Finish Date]");
          DomainConstants.MULTI_VALUE_LIST.remove("from[Assigned Tasks].to.current");
          DomainConstants.MULTI_VALUE_LIST.remove("from[Assigned Tasks].to.to[Project Access Key].from.from[Project Access List].to.name");
          DomainConstants.MULTI_VALUE_LIST.remove("from[Assigned Tasks].to.to[Project Access Key].from.from[Project Access List].to.current");

    }
	// Added by Vinit - Start 29-Jun-2018
	public String calculateSlipDays(Context context, String [] args, String strEstFinishDate, String strTaskStartDate, String strDuration, String strPercentComplete) throws Exception
    {
		${CLASS:emxTask} doEMXTask = new ${CLASS:emxTask}(context, args); 
		Map returnMAP = doEMXTask.getTaskStatusSlip(context, strEstFinishDate, strTaskStartDate, strDuration, strPercentComplete);
		
		String strSlipDays = (String)returnMAP.get("slipdays");
		
		return strSlipDays;
	}
	
   // 24-Feb-2016   |   Ajit    |  Set cron job on thursday to send mail regading Delayed Task Person Information List to Prakhar and Puran singh -- End
   /* public String calculateSlipDays(Context context, String strTaskId, boolean bIsLeafLevelTask, String strTaskEstimatedFinishDate) throws Exception

    {
        FileWriter detailsLog1 = new FileWriter("/////appv62013x//Scripts//logs//calculateSlipDays.log");          
        detailsLog1.write("\n.. MSILUtility : calculateSlipDays() -- Start >>.");detailsLog1.flush();
        detailsLog1.write("\n strTaskId >> "+strTaskId);detailsLog1.flush();
        detailsLog1.write("\n bIsLeafLevelTask >> "+bIsLeafLevelTask);detailsLog1.flush();
        detailsLog1.write("\n strTaskEstimatedFinishDate >> "+strTaskEstimatedFinishDate);detailsLog1.flush();

        String strSlipDays ="0";

        com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

        com.matrixone.apps.program.Task childTask = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);

        try

        {

            if(null != strTaskId && strTaskId.length() > 0)

            {

                ArrayList<Long> slipDaysList = new ArrayList<Long>();



                java.util.Date todayDate = new java.util.Date();

                java.util.Date estFinishDate = new java.util.Date();

                long lSlipDay                           = 0;

                long lSlipDayAbsolute                   = 0;



                task.setId(strTaskId);



                if(!bIsLeafLevelTask)

                {
                    detailsLog1.write("\n inside if >> ");detailsLog1.flush();
                    
                    StringList slObjSelectList = new StringList();

                    slObjSelectList.addElement(task.SELECT_ID);

                    slObjSelectList.addElement(task.SELECT_CURRENT);

                    slObjSelectList.addElement(task.SELECT_TYPE);

                    slObjSelectList.addElement(task.SELECT_NAME);

                    slObjSelectList.addElement(task.SELECT_TASK_ACTUAL_FINISH_DATE);

                    slObjSelectList.addElement(task.SELECT_TASK_ESTIMATED_FINISH_DATE);

                    slObjSelectList.addElement(task.SELECT_HAS_SUBTASK);


                    MapList childTasksList = getTasks(context, task, 0, slObjSelectList, null);
                    detailsLog1.write("\n childTasksList if >> "+childTasksList.size());detailsLog1.flush();

                    if(null != childTasksList && childTasksList.size() > 0)

                    {
                        
                        int nChildTaskListSize = childTasksList.size();

                        for(int nChildTaskCount = 0; nChildTaskCount < nChildTaskListSize; nChildTaskCount++)

                        {

                            Map childTaskMap = (Map)childTasksList.get(nChildTaskCount);

                            String strChildTaskHasSubtask = (String)childTaskMap.get(SELECT_HAS_SUBTASK);                           

                            detailsLog1.write("\n strChildTaskHasSubtask if >> "+strChildTaskHasSubtask);detailsLog1.flush();

                            if(null != strChildTaskHasSubtask && !"True".equalsIgnoreCase(strChildTaskHasSubtask)) // if the child task is the leaf level task

                            {
                                
                                childTask.setId((String)childTaskMap.get(task.SELECT_ID));

                                String strChildTaskCurrent = (String)childTaskMap.get(task.SELECT_CURRENT);  
                                detailsLog1.write("\n strChildTaskCurrent if >> "+strChildTaskCurrent);detailsLog1.flush();
                                if(null != strChildTaskCurrent && !"Complete".equalsIgnoreCase(strChildTaskCurrent))

                                {
                                    
                                    String strChildTaskEstimatedFinishDate = (String)childTaskMap.get(task.SELECT_TASK_ESTIMATED_FINISH_DATE); // IF TASK EST. FINISH DATE HAS PASSED
                                    detailsLog1.write("\n strChildTaskEstimatedFinishDate if >> "+strChildTaskEstimatedFinishDate);detailsLog1.flush();
                                    //if the task is incomplete use the Estimated Finish date for slip days calculations

                                    estFinishDate  = sdf.parse(strChildTaskEstimatedFinishDate);

                                    detailsLog1.write("\n estFinishDate if >> "+estFinishDate);detailsLog1.flush();

                                    estFinishDate.setHours(0);

                                    estFinishDate.setMinutes(0);

                                    estFinishDate.setSeconds(0);

                                    todayDate.setHours(0);

                                    todayDate.setMinutes(0);

                                    todayDate.setSeconds(0);

                            //  detailsLog1.write("\n estFinishDate if >> "+estFinishDate);detailsLog1.flush();

                                    if (estFinishDate.before(todayDate)) 

                                    {

                                        //calculate the slip days and change color according to the amount of days

                                        //the milestone (task) has slipped
                                        
                                        lSlipDay = childTask.computeDuration(estFinishDate,todayDate) - 1;//take out the starting day
                                        
                                        lSlipDayAbsolute = java.lang.Math.abs(lSlipDay);

                                    }   
                                    
                                    slipDaysList.add(lSlipDayAbsolute);                                 
                                    
                                }                                   

                            }

                        }

                    }
            //  slipDaysList.add(lSlipDayAbsolute);
                }

                else

                {
                detailsLog1.write("\n inside else >> ");detailsLog1.flush();
                    //if the task is incomplete use the Estimated Finish date for slip days calculations

                    estFinishDate  = sdf.parse(strTaskEstimatedFinishDate);

                    

                    estFinishDate.setHours(0);

                    estFinishDate.setMinutes(0);

                    estFinishDate.setSeconds(0);

                    todayDate.setHours(0);

                    todayDate.setMinutes(0);

                    todayDate.setSeconds(0);
                    detailsLog1.write("\n estFinishDate >> "+estFinishDate);detailsLog1.flush();
                    
                    if (estFinishDate.before(todayDate)) 

                    {

                        //calculate the slip days according to the amount of days the milestone (task) has slipped

                        //the milestone (task) has slipped
                        
                        lSlipDay = childTask.computeDuration(estFinishDate,todayDate) - 1;//take out the starting day 
                    detailsLog1.write("\n lSlipDay >> "+lSlipDay);detailsLog1.flush();

                        lSlipDayAbsolute = java.lang.Math.abs(lSlipDay);
                    detailsLog1.write("\n lSlipDayAbsolute >> "+lSlipDayAbsolute);detailsLog1.flush();

                    }   
                    
                    slipDaysList.add(lSlipDayAbsolute); 

                }                                                                           
                 detailsLog1.write("\n..slipDaysList..>>"+slipDaysList);detailsLog1.flush();
                 if(null !=slipDaysList && slipDaysList.size()>0){
                long largestSlipValue = Collections.max(slipDaysList);   
                detailsLog1.write("\n..largestSlipValue..>>"+largestSlipValue);detailsLog1.flush();
                strSlipDays = String.valueOf(largestSlipValue);
                 }else{
                     strSlipDays="0";
                 }

            }

        }catch (Exception ex)

        {

            //throw new FrameworkException();
           detailsLog1.write("ex.........>>"+ex);detailsLog1.flush();

        }


detailsLog1.write("\n..strSlipDays..>>"+strSlipDays);detailsLog1.flush();
        return strSlipDays;

    } */
	
	// Added by Vinit - End 29-Jun-2018

    // 24-Feb-2016   |   Ajit    |  Set cron job on thursday to send mail regading Delayed Task Person Information List to Prakhar and Puran singh -- Start

   /**
     *  This Method is to  delete files and folder from the workspace
     * @param context the eMatrix <code>Context</code> object.
     * @return void
     * @throws Exception if the operation fails.
     * @author Ajit
     */
        public void delete(File file)throws Exception 
        {
                if(file.isDirectory()){
                        //directory is empty, then delete it
                        if(file.list().length==0){
                                file.delete();       
                        }else{
                                //list all the directory contents
                                String files[] = file.list();
                                for (String temp : files) {
                                        //construct the file structure
                                        File fileDelete = new File(file, temp);
                                        //recursive delete
                                        delete(fileDelete);
                                }
                                //check the directory again, if empty then delete it
                                if(file.list().length==0){
                                        file.delete();                                            
                                }
                        }
                }else{
                        //if file, then delete it
                        file.delete();
                }
        }


// 24-Feb-2016   |   Ajit    |  Set cron job on thursday to send mail regading Delayed Task Person Information List to Prakhar and Puran singh -- End

// 28/04/2016    |   Ajit    |  To transfer Current And Future Task And Route of the old person to new person --Start

/**
 *  This Method is to  transfer Current And Future Task And Route of the old person to new person
 * @param context the eMatrix <code>Context</code> object.
 * @return void
 * @throws Exception if the operation fails.
 * @author Ajit
 */
public HashMap transferOwnership(Context context, String[] args) throws Exception
    {

    HashMap returnMap   = new HashMap();
        try
        {
            HashMap programMap          =   (HashMap)JPO.unpackArgs(args);
            Map requestMap              =   (Map)programMap.get("requestMap");
            String strTransObjName      =   (String)requestMap.get("RRFQ/RFQ Name");
            String strOldPerson         =   (String)requestMap.get("From");
            String strNewPerson         =   (String)requestMap.get("To");
            if(strOldPerson == null || strNewPerson == null)
            {
                returnMap.put("Action", "Stop");
                returnMap.put("Message", "Please Provide Valid From / To Person.");
                return returnMap;
            }else{
                strOldPerson = strOldPerson.trim();
                strNewPerson = strNewPerson.trim();
            }
            StringList slObjectSelect = new StringList();
            slObjectSelect.addElement(DomainConstants.SELECT_ID);
            slObjectSelect.addElement(DomainConstants.SELECT_TYPE);
            slObjectSelect.addElement(DomainConstants.SELECT_NAME);
            slObjectSelect.addElement(DomainConstants.SELECT_CURRENT);
            slObjectSelect.addElement("attribute[MT]");
            slObjectSelect.addElement("attribute[SC Owner]");
            slObjectSelect.addElement("attribute[EN Owner]");
            slObjectSelect.addElement("attribute[Co-Owners]");
            slObjectSelect.addElement("attribute[originator]");
            slObjectSelect.addElement("owner");
            StringList slPersonSelect = new StringList();
            slPersonSelect.addElement(DomainConstants.SELECT_ID);
        
            MapList mlOldPersonList =  DomainObject.findObjects(context,
                    DomainConstants.TYPE_PERSON,
                    strOldPerson.trim(),
                    "*",
                    "*",
                    "*",
                    "",
                    false,
                    slPersonSelect);
            MapList mlNewPersonList =  DomainObject.findObjects(context,
                    DomainConstants.TYPE_PERSON,
                    strNewPerson.trim(),
                    "*",
                    "*",
                    "*",
                    "current=='Active'",
                    false,
                    slPersonSelect);
            // UI Validation -- Start
          
            if(null != mlOldPersonList && mlOldPersonList.size()==0)
            {
                returnMap.put("Action", "Stop");
                returnMap.put("Message", "From Person '"+strOldPerson+"' is either Invalid or not Active");
                return returnMap;
            }
            if(null != mlNewPersonList && mlNewPersonList.size()==0)
            {
                returnMap.put("Action", "Stop");
                returnMap.put("Message", "To Person '"+strNewPerson+"' is either Invalid or not Active");
                return returnMap;
            }
            if(strNewPerson.equals(strOldPerson))
            {
                returnMap.put("Action", "Stop");
                returnMap.put("Message", "From and To Person Should be Different");
                return returnMap;
            }            
            // UI Validation -- End            

            String [] sArgs=new String [2];         
            sArgs[0]=strOldPerson;
            sArgs[1]=strNewPerson;
            ContextUtil.startTransaction(context, true);
            ContextUtil.pushContext(context, "Test Everything", DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING); 
            transferCurrentAndFutureTaskAndRoute(context,sArgs);
            ContextUtil.popContext( context);            

        }catch (Exception ex)
        { 
            ContextUtil.abortTransaction(context);              
            ex.printStackTrace();                          
            returnMap.put("Action", "Stop");
            returnMap.put("Message", "Error While Transfer :: "+ex.getMessage()+" So Transaction Got Aborted");
            return returnMap;
        }

            returnMap.put("Action", "continue");
            returnMap.put("Message", "Ownership Transfered Successfully");
            // If Sucessfully executed  then Commited to DB
            ContextUtil.commitTransaction(context);
        
        return returnMap;
    }

public void  transferCurrentAndFutureTaskAndRoute(Context context, String [] args) throws Exception
    {
        System.out.println("\n\n Enter transferCurrentAndFutureTaskAndRoute() !!!!!!!!! -- Start");

        try
        {
            String strOldPerson =args[0]; // Old Person name
            String strNewPerson =args[1]; // New Person name
            // Added by Ajit -- 03/30/2015 -- To Transfer Future Tasks of the Routes which are in Define State -- Start
            StringList slSelects = new StringList();
            slSelects.addElement(DomainConstants.SELECT_NAME);
            slSelects.addElement(DomainConstants.SELECT_ID);
            slSelects.addElement(DomainConstants.SELECT_CURRENT);          
            slSelects.addElement("from[Project Route].to.name");
            slSelects.addElement("from[Route Node].to.name");          
            //String sWhereExp = "(current!='Complete' && current!='Archive') && (from[Route Node].to.name ~~ '"+strOldPerson+"')";
            String sWhereExp = "((current=='Define' || current=='In Process') && (from[Route Node].to.name ~~ '"+strOldPerson+"'))";
            System.out.println("\n sWhereExp !!!"+sWhereExp);
            MapList mlRoute = DomainObject.findObjects(context,
                    "Route",
                    "*",
                    sWhereExp,
                    slSelects);
            System.out.println("\n No Of Route !!!"+mlRoute.size());            
            if(null != mlRoute && mlRoute.size()>0){           
                for(int r=0;r<mlRoute.size();r++){
                    Map mapRouteInfo = (Map)mlRoute.get(r);
                    String sRouteId =(String) mapRouteInfo.get(DomainConstants.SELECT_ID);
                    //Method to Transfer Route details 
                    //System.out.println("\n Transfering Route Details !!!");                 
                    StringList slObjectSelects = new StringList();
                    slObjectSelects.add(DomainConstants.SELECT_ID);    
                    slObjectSelects.add(DomainConstants.SELECT_NAME);
                    slObjectSelects.add(DomainConstants.SELECT_CURRENT);          
                    DomainObject domRoute = null;
                    if(!"".equals(sRouteId)&& !"null".equals(sRouteId)&& null!=sRouteId){
                        domRoute = DomainObject.newInstance(context,sRouteId);
                    }
                    if(null != domRoute ){
                        //Get all tasks connected to Route where Task Assignee is old Person
                        String sInboxtaskWhereExp="current=='Assigned' && owner=='"+strOldPerson+"'";
                        MapList mlInboxlist = domRoute.getRelatedObjects(context,"Route Task","Inbox Task",slObjectSelects,null,true,false,(short)1,sInboxtaskWhereExp,"");
                         //System.out.println("\n mlInboxlist !!!"+mlInboxlist.size());
                        if(null!=mlInboxlist && !mlInboxlist.isEmpty())
                        {
                            for(int k=0;k<mlInboxlist.size();k++)
                            {
                                Map mapIT = (Map) mlInboxlist.get(k);
                                String strInboxId=(String)mapIT.get(DomainConstants.SELECT_ID); 
                                String aMethodArgs[] = new String[3];                                
                                aMethodArgs[0] =strInboxId ;
                                aMethodArgs[1] =strOldPerson ;
                                aMethodArgs[2] =strNewPerson ;
                                //System.out.println("\n Ready to call  taskTransfer() !!!"); 
                                 //call method taskTransfer
                                 taskTransfer(context,aMethodArgs);  
                                 
                            }
                        }
                        // To Grant and Revoke from Old Person to New person if anything Exist -- Start
                        StringList slGranteeList = domRoute.getGrantees(context);
                        if(null != slGranteeList && slGranteeList.contains(strOldPerson))
                        {
                            ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                            //String strRevokeCommand = "mod bus "+sRouteId+" revoke grantee "+strOldPerson;
                            //MqlUtil.mqlCommand(context,strRevokeCommand);
                            String strGrantCommand = "mod bus "+sRouteId+" grant "+strNewPerson+" access all";
                            MqlUtil.mqlCommand(context,strGrantCommand);    
                            ContextUtil.popContext( context);
                        } 
                        // To Grant and Revoke from Old Person to New person if anything Exist -- End
                        // To Modify connection of Incomplete Route Object from Old Person to New person if anything Exist -- Start
                        // To Modify the "Route Node" connected person if it is having old person and Task will be created in future -- Start
                        StringList slRelSelect = new StringList();
                        slRelSelect.addElement("id[connection]");
                        String relWhere = "attribute[Actual Completion Date]==''";
                        MapList mlRoutePerson = domRoute.getRelatedObjects(context,"Route Node",DomainConstants.TYPE_PERSON,slObjectSelects,slRelSelect,true,true,(short)1,null,relWhere);
                        if(null != mlRoutePerson && mlRoutePerson.size()>0){
                            for(int route=0;route<mlRoutePerson.size();route++){
                                Map mapRoutePerson = (Map) mlRoutePerson.get(route);
                                String sRoutePersonName = (String)mapRoutePerson.get(DomainConstants.SELECT_NAME);
                                if((!"".equals(sRoutePersonName)&& !"null".equals(sRoutePersonName)&& null!=sRoutePersonName) && strOldPerson.equals(sRoutePersonName)){
                                    String sRoutePersonRelId = (String) mapRoutePerson.get("id[connection]");
                                    ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                                    String strROutePersonMQL = "mod connection "+sRoutePersonRelId+" to Person "+strNewPerson+" -";                                             
                                    MqlUtil.mqlCommand(context,strROutePersonMQL);                                              
                                    ContextUtil.popContext( context);
                                }
                            }
                        }
                        // To Modify the "Route Node" connected person if it is having old person and Task will be created in future -- End
                        // To Modify connection of Incomplete Route Object from Old Person to New person if anything Exist -- End
                    }
                }
            }
            // Added by Ajit -- 03/30/2015 -- To Transfer Future Tasks of the Routes which are in Define State -- End       
        }catch (Exception ex)
        {
            System.out.println("\n\n Error in transferCurrentAndFutureTaskAndRoute() !!!!!!!!! "+ex.getMessage());
        }
        System.out.println("\n\n Exit successfully transferCurrentAndFutureTaskAndRoute() !!!!!!!!! -- End");
   }

   // 28/04/2016    |   Ajit    |  To transfer Current And Future Task And Route of the old person to new person -- End

	// 01-Jun-2016   |   Dheeraj    |  To transfer Current And Future Task And Route of the old person to new person --Start

	/**
	 *  This Method is to  transfer Current And Future Task And Route of the old person to new person
	 * @param context the eMatrix <code>Context</code> object.
	 * @return void
	 * @throws Exception if the operation fails.
	 * @author Ajit
	 */
	public HashMap transferOwnershipByProject(Context context, String[] args) throws Exception
    {

        HashMap returnMap   = new HashMap();
        try
        {
            HashMap programMap          =   (HashMap)JPO.unpackArgs(args);
            Map requestMap              =   (Map)programMap.get("requestMap");
            String strOldPerson         =   (String)requestMap.get("From");
            String strNewPerson         =   (String)requestMap.get("To");
            String strProject           =   (String)requestMap.get("ProjectDisplay");
            if(strOldPerson == null || strNewPerson == null)
            {
                returnMap.put("Action", "Stop");
                returnMap.put("Message", "Please Provide Valid From / To Person.");
                return returnMap;
            }else{
                strOldPerson = strOldPerson.trim();
                strNewPerson = strNewPerson.trim();
                strProject   = strProject.trim();
            }
            
            StringList slPersonSelect = new StringList();
            slPersonSelect.addElement(DomainConstants.SELECT_ID);
        
            MapList mlOldPersonList =  DomainObject.findObjects(context, DomainConstants.TYPE_PERSON, strOldPerson.trim(), "*", "*", "*", "", false, slPersonSelect);
            MapList mlNewPersonList =  DomainObject.findObjects(context, DomainConstants.TYPE_PERSON, strNewPerson.trim(), "*", "*", "*", "current=='Active'", false, slPersonSelect);
            // UI Validation -- Start
          
            if(null != mlOldPersonList && mlOldPersonList.size()==0)
            {
                returnMap.put("Action", "Stop");
                returnMap.put("Message", "From Person '"+strOldPerson+"' is either Invalid or not Active");
                return returnMap;
            }
            if(null != mlNewPersonList && mlNewPersonList.size()==0)
            {
                returnMap.put("Action", "Stop");
                returnMap.put("Message", "To Person '"+strNewPerson+"' is either Invalid or not Active");
                return returnMap;
            }
            if(strNewPerson.equals(strOldPerson))
            {
                returnMap.put("Action", "Stop");
                returnMap.put("Message", "From and To Person Should be Different");
                return returnMap;
            }            
            // UI Validation -- End 

            ContextUtil.startTransaction(context, true);
            ContextUtil.pushContext(context); 
            transferCurrentAndFutureTaskAndRouteForProject(context, strOldPerson, strNewPerson, strProject);
            ContextUtil.popContext( context);

        }catch (Exception ex)
        { 
            ContextUtil.abortTransaction(context);
            ex.printStackTrace();
            returnMap.put("Action", "Stop");
            returnMap.put("Message", "Error While Transfer :: "+ex.getMessage()+" So Transaction Got Aborted");
            return returnMap;
        }

        returnMap.put("Action", "continue");
        returnMap.put("Message", "Ownership Transfered Successfully");
        // If Successfully executed  then Committed to DB
        ContextUtil.commitTransaction(context);
        
        return returnMap;
    }

    public void  transferCurrentAndFutureTaskAndRouteForProject(Context context, String strOldPerson, String strNewPerson, String strProjects) throws Exception
    {
        try
        {
        	DomainConstants.MULTI_VALUE_LIST.add("to[Route Task].from.id");
            DomainConstants.MULTI_VALUE_LIST.add("from[Route Node].id");
            
            StringList slSelects = new StringList();
            slSelects.addElement(DomainConstants.SELECT_ID);
            slSelects.addElement("to[Object Route].from.to[Project Access Key].from.from[Project Access List].to.name");
            slSelects.addElement("to[Route Task|from.owner=="+strOldPerson+" && from.current=='Assigned'].from.id");
            slSelects.addElement("from[Route Node|to.name=='"+strOldPerson+"'].id");
            
            String sWhereExp = "((current=='Define' || current=='In Process') && (from[Route Node].to.name ~~ '"+strOldPerson+"'))";
            MapList mlRoute = DomainObject.findObjects(context, "Route", "*", sWhereExp, slSelects);
            int iRouteSize = mlRoute.size();
            if(null != mlRoute && iRouteSize>0)
            {
                for(int r=0;r<iRouteSize;r++) {
                    DomainObject domRoute = null;
                    Map routeMap = (Map)mlRoute.get(r);
                    String sRouteId =(String) routeMap.get(DomainConstants.SELECT_ID);
                    if(!"".equals(sRouteId)&& !"null".equals(sRouteId)&& null!=sRouteId){
                        domRoute = DomainObject.newInstance(context,sRouteId);
                        if(null == domRoute) continue;
                    }
                    
                    // Match whether route is of selected project.
                    String strProjectName = (String) routeMap.get("to[Object Route].from.to[Project Access Key].from.from[Project Access List].to.name");
                    if(null == strProjects || null == strProjectName || "".equals(strProjects) || !strProjects.equalsIgnoreCase(strProjectName))
                            continue;
                    
                    //Get all tasks connected to Route where Task Assignee is old Person
                    StringList slInboxTasks = (StringList) routeMap.get("to[Route Task].from.id");
                    if(null!=slInboxTasks && !slInboxTasks.isEmpty()){
						int iInboxTaskSize = slInboxTasks.size();
                        for(int k=0;k<iInboxTaskSize;k++){
                            String aMethodArgs[] = {(String)slInboxTasks.get(k), strOldPerson, strNewPerson};
                            taskTransfer(context,aMethodArgs);  
                        }
                    }
                    
                    // To Grant and Revoke from Old Person to New person if anything Exist
                    StringList slGranteeList = domRoute.getGrantees(context);
                    if(null != slGranteeList && slGranteeList.contains(strOldPerson)){
                        ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), "", "");
                        MqlUtil.mqlCommand(context,"mod bus "+sRouteId+" grant "+strNewPerson+" access all");    
                        ContextUtil.popContext( context);
                    } 
                    
                    // To Modify the "Route Node" connected person if it is having old person and Task will be created in future
                    StringList slRoutePersonRelId = (StringList)routeMap.get("from[Route Node].id");
                    if(null!=slRoutePersonRelId && !slRoutePersonRelId.isEmpty()){
						int iRoutePersonRelSize = slRoutePersonRelId.size();
                        for(int k=0;k<iRoutePersonRelSize;k++){
                            ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), "", "");
                            MqlUtil.mqlCommand(context, "mod connection "+slRoutePersonRelId.get(k)+" to Person "+strNewPerson+" -");
                                ContextUtil.popContext( context);
                            }
                        }
                    }
            }
        }catch (Exception ex)
        {
            System.out.println("\n\n Error in transferCurrentAndFutureTaskAndRouteForProject() !!!!!!!!! "+ex.getMessage());
        }
        finally
        {
            DomainConstants.MULTI_VALUE_LIST.remove("to[Route Task].from.id");
            DomainConstants.MULTI_VALUE_LIST.remove("from[Route Node].id");
        }
    }
}
