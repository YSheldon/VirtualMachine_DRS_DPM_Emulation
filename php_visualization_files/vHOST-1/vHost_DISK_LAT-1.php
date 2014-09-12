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
    
    $disktable = array();
	$disktable['cols'] = array(

    array('label' => 'timestamp', 'type' => 'string'),
    array('label' => 'DiskLat', 'type' => 'number')

);
    while($row=mysql_fetch_assoc($result))
    {
   	$vmname = $row['vmname'];
    $temp = array();
    $temp[] = array('v' => (string)($row['time_val'])); 
    $temp[] = array('v' => (float) $row['disk_maxTotalLatency']); 
    $rows[] = array('c' => $temp);
        
    }
    $disktable['rows'] = $rows;
   $jsonDiskTable =json_encode($disktable);
    
    //echo "VM-Name: ",$vmname;

	}
?>


<html>
<head>
    <title>Disk Latency</title>
    
    <!--Load the AJAX API-->
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript" src="jquery-2.1.0.min.js"></script>
    <script type="text/javascript">
    
        google.load('visualization', '1', {'packages':['corechart']});
        google.setOnLoadCallback(drawChart);

        function drawChart() {
        	
            var data = new google.visualization.DataTable(<?=$jsonDiskTable?>);

            var options = {
                title: 'DISK LATENCY of vHost-1',
                curveType: 'function',
                backgroundColor: '#CCCCCC',
                
                
                vAxis: {title: 'Disk Latency (msec)',
                gridlines: {color: "#6AB5D1"},
    			baselineColor: '#6AB5D1'}, 
    			
    			hAxis: {title: 'Time stamp(hr:min:sec)',
    			gridlines: {color: "#6AB5D1"},
    			baselineColor: '#6AB5D1'}
                
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
