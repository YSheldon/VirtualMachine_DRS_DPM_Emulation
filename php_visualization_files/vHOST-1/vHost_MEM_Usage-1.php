<?php
    $db = mysql_connect("cmpe272.cn1huhw7fmgm.us-west-1.rds.amazonaws.com:3306", "root", "rootcmpe");
	mysql_select_db("CMPE283");   
	
$result =mysql_query("SELECT * FROM CMPE283.VHOST_Usage WHERE vmname = 'T13-vHost02-cum3-2GB-NFS2-lab3_base5_.133.13'");

if (!$result)
    {

        echo '<tr>';
        echo '<td>No data found</td>';
        echo '</tr>';

    }
    else
    {
    
    
	$memtable = array();
	$memtable['cols'] = array(

    // Labels for your chart, these represent the column titles
    // Note that one column is in "string" format and another one is in "number" format as pie chart only required "numbers" for calculating percentage and string will be used for column title
    array('label' => 'time_val', 'type' => 'string'),
    array('label' => 'MemUsed', 'type' => 'number')

);
    while($row=mysql_fetch_assoc($result))
    {
   	$vmname = $row['vmname'];
    $temp = array();
    // the following line will be used to slice the Pie chart
    $temp[] = array('v' => (string)($row['time_val']));

    // Values of each slice
    $temp[] = array('v' => (float) $row['mem_consumed']/(1024*1024)); 
    $rows[] = array('c' => $temp);
        
    }
    $memtable['rows'] = $rows;
    //echo "VM-Name: ",$vmname;
   $jsonMemTable =json_encode($memtable);


	}
?>


<html>
<head>
    <title>Memory Usage</title>
    
    <!--Load the AJAX API-->
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript" src="jquery-2.1.0.min.js"></script>
    <script type="text/javascript">
    
        google.load('visualization', '1', {'packages':['corechart']});
        google.setOnLoadCallback(drawChart);

        function drawChart() {
        	
            var data = new google.visualization.DataTable(<?=$jsonMemTable?>);

            var options = {
                title: 'MEMORY USAGE of vHost-1',
                curveType: 'function',
                backgroundColor: '#CCCCCC',
                
                vAxis: {title: 'Memory Consumed (GB)',
                		viewWindowMode:'explicit',
                		viewWindow:{min:0.0},
                		gridlines: {color: "#6AB5D1"},
                		
    			baselineColor: '#6AB5D1'},
                 hAxis: {title: 'Time stamp (hr:min:sec)',
                 gridlines: {color: "#6AB5D1"},
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
