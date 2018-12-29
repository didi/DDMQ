#include "json/json.h"
#include <stdio.h>
#include <string>

int main( int argc, char**argv)
{
	if ( argc != 2 ) 
	{
		printf("error argc\n");
		return -1;
	}
	FILE* in=fopen( argv[1], "r");
	if ( NULL == in ) 
	{
		printf("error open %s\n", argv[1]);
		return -1;
	}
	char buffer[ 4096];
	int len = fread( buffer, 1, 1024, in);


	Json::Reader reader;  
    	Json::Value root;  
	if (reader.parse(buffer, buffer+len, root))  
	{
		printf( "error parse\n");
		return -1;
	}
	std::string t1_v=root["t1"].asString();
	printf( "t1=%s\n", t1_v.c_str());
	std::string t2_v=root["t2"].asString();
	printf( "t2=%s\n", t2_v.c_str());
	if ( root[ "t3"].isNull())
	{
		printf( "t3 not exist \n");
	}
	return 0;	
}
