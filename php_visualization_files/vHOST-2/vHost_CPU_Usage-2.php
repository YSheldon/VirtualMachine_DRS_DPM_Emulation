<?php
    $db = mysql_connect("cmpe272.cn1huhw7fmgm.us-west-1.rds.amazonaws.com:3306", "root", "rootcmpe");
	mysql_select_db("CMPE283");   
	
$result =mysql_query("SELECT * FROM CMPE283.VHOST_Usage WHERE vmname = 'T13-vHost02-cum3-2GB-NFS2-lab3_base5_.133.12'");

/*
select a.time_stamp,a.cpu_usage as cpu_vm1, b.cpu_usage as cpu_vm2 from
  (select * from CMPE283.VM_Usage where vmname='T13-vHost01-cum3-2GB-NFS2-lab3_base5_.133.11') a
join
  (select * from CMPE283.VM_Usage where vmname='T13-vHost01-cum3-2GB-NFS2-lab3_base5_.133.13') b
on a.time_stamp = b.time_stamp;
*/

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
    array('label' => 'CpuUsed', 'type' => 'number')

);
    while($row=mysql_fetch_assoc($result))
    {
   	$vmname = $row['vmname'];
    $temp = array();
    // the following line will be used to slice the Pie chart
    $temp[] = array('v' => (string)($row['time_val'])); 

    // Values of each slice
    $temp[] = array('v' => (float) $row['cpu_usage']/100); 
    $rows[] = array('c' => $temp);
        
    }
    $cputable['rows'] = $rows;
   $jsonCpuTable =json_encode($cputable);


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
                title: 'CPU USAGE of vHost-2',
                curveType: 'function',
                backgroundColor: '#CCCCCC',
                 animation:{
        				duration: 1000,
       					 easing: 'out',
     					 },
                
                vAxis: {
                	title: 'CPU Usage (%)',
        	  		gridlines: {color: "#6AB5D1"},
    				baselineColor: '#6AB5D1'
    					},
    					
                hAxis: {
                title: 'Time stamp (hr:min:sec)',
    			baselineColor: '#6AB5D1'
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
    <div id="chart_div" style="width: 600px; height: 300px;">
    </div>
</body>
</html>
