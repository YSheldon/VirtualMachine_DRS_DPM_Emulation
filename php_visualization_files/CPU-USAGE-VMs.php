<?php
    $db = mysql_connect("cmpe272.cn1huhw7fmgm.us-west-1.rds.amazonaws.com:3306", "root", "rootcmpe");
   //$db = mysql_connect("130.65.157.136:3306", "root", "guru");
	mysql_select_db("CMPE283");   
	//mysql_select_db("Log"); 
//$result =mysql_query("SELECT time_val,cpu_usage as cpu_vm1,vmname as vm1 FROM CMPE283.VM_Usage WHERE vmname = 'Team13_VM1'");


$result = mysql_query("
SELECT t.time_val,
         MAX(CASE WHEN t.vmname = 'Team13_VM1' THEN t.cpu_usage ELSE NULL END) AS vm1,
         MAX(CASE WHEN t.vmname = 'Team13_VM2' THEN t.cpu_usage ELSE NULL END) AS vm2
    FROM VM_Usage t
GROUP BY t.time_val   
");


if (!$result)
    {

        echo '<tr>';
        echo '<td>No data found</td>';
        echo '</tr>';

    }
    else
    {
    
    $cputable = array();
	$cputable['cols'] = array(

    // Labels for your chart, these represent the column titles
    // Note that one column is in "string" format and another one is in "number" format as pie chart only required "numbers" for calculating percentage and string will be used for column title
    array('label' => 'time_val', 'type' => 'string'),
    array('label' => 'vm1', 'type' => 'number'),
    array('label' => 'vm2', 'type' => 'number')

);

$max1=0;
$max2=0;
$min1=100;
$min2=100;

    while($row=mysql_fetch_assoc($result))
    {
    
   	$vmname1 = 'Team13_VM1';
   	$vmname2 = 'Team13_VM2';
    $temp = array();
    // the following line will be used to slice the Pie chart
    $temp[] = array('v' => (string)($row['time_val'])); 

    // Values of each slice
    if($row['vm1'] > 0){
    $temp[] = array('v' => (float) $row['vm1']/100); 
    $prev1 = (float) ($row['vm1']/100);
    if( ((float) $row['vm1']/100) < $min1)
     	$min1=((float) $row['vm1']/100);
    }
    else 
    $temp[] = array('v' => $prev1); 
    
    if($row['vm2'] > 0){
    $temp[] = array('v' => (float) $row['vm2']/100);
    $prev2 = (float) ($row['vm2']/100);
    
    if( ((float) $row['vm2']/100) < $min2)
     	$min2=((float) $row['vm2']/100);
    }
    else 
    $temp[] = array('v' => $prev2); 
    
    
    $rows[] = array('c' => $temp);
    
    if( ((float) $row['vm1']/100)> $max1)
     	$max1=((float) $row['vm1']/100);
     	     	
        
    if( ((float) $row['vm2']/100)> $max2){
     	$max2=((float) $row['vm2']/100);
     	}
     	
    
    }
    $cputable['rows'] = $rows;
   $jsonCpuTable =json_encode($cputable);
  // echo $jsonCpuTable;
	}
?>


<html>
<head>
    <title>CPU Usage</title>
    
    <!--Load the AJAX API-->
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript" src="jquery-2.1.0.min.js"></script>
    <script type="text/javascript">
    
        google.load('visualization', '1', {'packages':['corechart']});
        google.setOnLoadCallback(drawChart);

        function drawChart() {
        	
            var data = new google.visualization.DataTable(<?=$jsonCpuTable?>);
			//data.addColumn({type: 'string', role: 'annotation'});
            var options = {
                title: 'CPU USAGE in VMs of Host-1',
                curveType: 'function',
                backgroundColor: '#CCCCCC',
                
                vAxis: {
                	title: 'CPU Usage (%)',
        	  		gridlines: {color: "#6AB5D1"},
    				baselineColor: '#6AB5D1'
    					},
    					
                hAxis: {
                title: 'Time stamp (hr:min)',
    			baselineColor: '#6AB5D1',
    			gridlines: {color: "#6AB5D1"},
                		}
                
            };

            var chart = new google.visualization.AreaChart(
                        document.getElementById('chart_div'));
            
            chart.draw(data, options);
            
            
        }

    </script>
    <META HTTP-EQUIV="refresh" CONTENT="10">
</head>
<body>
	
	Virtual Machine Name : <b><?=$vmname1?></b>
	<br>
	Max CPU value : <b><?=$max1?>%</b><br>
	Min CPU value :<b><?=$min1?>%</b><br>
	<br>
	Virtual Machine Name : <b><?=$vmname2?></b>
	<br>
	Max CPU value : <b><?=$max2?>%</b><br>
	Min CPU value :<b><?=$min2?>%</b><br>
    <div id="chart_div" style="width: 900px; height: 500px;">
    </div>
</body>
</html>
