//package python;
//
//import java.util.List;
//
//public class TopsPrint {
//
///* Hard coded but will change */
//int Radius;
//double Scale = 0.60;
//String buffer;
//
//boolean Small;
//
//SSE Root;
//
//int GridUnitSize;
//
//private double PSxy(double  v ) {
//    return ( (float) (v) / (float) GridUnitSize );
//}
//    
//private double iPSxy(double  v ) {
//    return ( (float) (v) * (float) GridUnitSize );
//}
//
//private final double VERT_SEP = 2.0;
//private final double BORDER = 5.0;
//private final String TITLE_PREF = "TOPS Cartoon: ";
//
//boolean PrintCartoons( int NCartoons, List<SSE> CartoonStarts, String FileName, String ProtName, PlotFragInformation pfi, int ErrorStat )
//{
//
//	int i;
//	SSE p;
//	double Xmax, Xmin, Ymax, Ymin;
//	double CartoonTransX, CartoonTransY;
//	double RubricSpace;
//	String Title;
//	int lt;
//
//	ErrorStat = 0;
//
//	if ( (NCartoons<1) || CartoonStarts.isEmpty() ) return false;
//
//	/* Open file */
//	if (!OpenPostscript( FileName ))
//		return false;
//
//	/* Find size of space required for cartoons */
//	CartoonTransX = NCartoons;
//	
//	CartoonTransY = NCartoons;
//	
//	FindTotSize(NCartoons,CartoonStarts,Xmin,Xmax,Ymin,Ymax,CartoonTransX,CartoonTransY);
//
//	/* Find space needed for rubric */
//	RubricSpace = 1.0 + (pfi.NFrags)*0.5;
//
//	/* Print bounding box command */
//	if (Small) {
//		PageSize( Xmax-Xmin+BORDER, Ymax-Ymin+BORDER+RubricSpace+VERT_SEP*(NCartoons-1));
//	} else {
//		PageSize( 21.9, 29.0 );
//	}
//
//	/* Define cartoon symbols */
//	DefCartoonSymbols();
//
//	/* Prepare plotting area */
//	PictureSize( Xmax-Xmin+BORDER, Ymax-Ymin+BORDER+RubricSpace+VERT_SEP*(NCartoons-1));
//	if (!Small) CentrePage();
//	ScalePage( Xmin-(BORDER/2.0), Xmax+(BORDER/2.0), Ymin-(BORDER/2.0)-VERT_SEP*(NCartoons-1), Ymax+RubricSpace+(BORDER/2.0) );
//	if (!Small) Perimeter();
//	ChooseFont( "Courier", ConvertXPoint(PSxy(Radius)) );
//	TextCentre( 0.5, 0.38 );
//	LineWidth( 0.01 );	
//
//	/* Print title */
//	lt = TITLE_PREF.length() + PROT_NAME_SIZE + 1;
//	if (lt>0)
//	{
//		Title = TITLE_PREF + ProtName;
//		PrintText(Title,(Xmax+Xmin)/2.0,Ymax+RubricSpace+(BORDER/4.0));
//	}
//
///* Print plot rubric */
//	PrintPlotRubric(pfi,(Xmax+Xmin)/2.0,Ymax+RubricSpace);
//
///* Print the Cartoons */
//	for ( i=0 ; i<NCartoons ; i++) 
//	{
//		Translate( PSxy(CartoonTransX[i]) , PSxy(CartoonTransY[i]) );
//		PrintCartoon(CartoonStarts[i]);
//	}
//
///* Done */
//	EndPostscript();
//
//	return true;
//}
//
///*
//  a function to find the total size of the cartoon diagrams
//  with the cartoons stacked in the y direction centred in the x direction on the centre of the first 
//*/
//void FindTotSize(int NCartoons, List<SSE> CartoonStarts, float Xmin, float Xmax, float Ymin, float Ymax, float CartoonTransX,
//															float CartoonTransY)
//{
//
//	int i,j;
//	SecStrPtr p;
//	float xmax,xmin,ymax,ymin;
//	float cxmax,cxmin,cymax,cymin;
//	float cyminp,tx;
//	float exwid,wid;
//
//	for ( i=0 ; i<NCartoons ; i++)
//	{
//		CartoonTransX[i] = 0.0;
//		CartoonTransY[i] = 0.0;
//	}
//
//	for ( i=0 ; i<NCartoons ; i++)
//	{
//
//		cyminp = cymin;
//
//        	for (p=CartoonStarts[i],cxmax=cxmin= (float) p.CartoonX,cymax=cymin= (float) p.CartoonY;p;p=p.To)
//        	{
//                	if (p.SymbolPlaced)
//                	{
//                        	if ((float) p.CartoonX>cxmax) cxmax = (float) p.CartoonX;
//                        	if ((float) p.CartoonX<cxmin) cxmin = (float) p.CartoonX;
//                        	if ((float) p.CartoonY>cymax) cymax = (float) p.CartoonY;
//                        	if ((float) p.CartoonY<cymin) cymin = (float) p.CartoonY;
//                	}
//        	}
//
//		if (i==0)
//		{
//			xmin = PSxy(cxmin);
//			xmax = PSxy(cxmax);
//			ymin = PSxy(cymin);
//			ymax = PSxy(cymax);
//			CartoonTransY[0] = 0.0;
//		}
//		else
//		{
//			wid = PSxy(cxmax-cxmin);
//			exwid = wid - xmax + xmin;
//			if ( exwid>0.0 )
//			{
//				xmin -= exwid/2.0;
//				xmax += exwid/2.0;
//			}
//			ymin -= PSxy(cymax-cymin);
//
//			CartoonTransY[i] -= (cymax+iPSxy(VERT_SEP)-cyminp);
//
//		}
//
//
//	}
//
//	*Xmin = xmin;
//	*Xmax = xmax;
//	*Ymin = ymin;
//	*Ymax = ymax;
//
//
//	tx = 0.0;
//	for ( i=0 ; i<NCartoons ; i++)
//	{
//
//        	for (p=CartoonStarts[i],cxmin=p.CartoonX;p;p=p.To)
//        	{
//                	if (p.SymbolPlaced)
//                	{
//                        	if ((float) p.CartoonX<cxmin) cxmin = (float) p.CartoonX;
//                	}
//        	}
//
//		CartoonTransX[i] = iPSxy(xmin)-cxmin-tx;
//
//		tx += CartoonTransX[i];
//
//	}
//
//	return;
//
//}
//
///*
//    Function to Print a rubric for the plot
//*/
//void PrintPlotRubric(PlotFragInfoPtr pfi, float xpos, float ypos)
//{
//
//	int i,j;
//	char buff[80];
//	float x,y;
//
//	x=xpos;
//	y=ypos;
//
//	for ( i=0 ; i<pfi.NFrags ; i++)
//	{
//
//		for( j=0 ; j<80 ; j++) buff[j] = ' ';
//		buff[79] = '\0';
//
//		buff[0] = 'N';
//		sprintf(buff+1,"%d",(i+1));
//		buff[4] =pfi.FragChainLims[i][0];
//		sprintf(buff+6,"%d",pfi.FragResLims[i][0]);
//		buff[12] = 'C';
//		sprintf(buff+13,"%d",(i+2));
//		buff[16] = pfi.FragChainLims[i][1];
//		sprintf(buff+18,"%d",pfi.FragResLims[i][1]);
//		for( j=0 ; j<22	; j++) if ( buff[j]=='\0' ) buff[j]=' ';
//		buff[22] = '\0';
//
//		PrintText(buff,x,y);
//		y=y-0.5;
//
//	}
//
//	return;
//}
//
///*
//  a function which prints a single Cartoon
//*/
//void PrintCartoon(SecStrPtr Start)
//{
//
//	int i,ncp;
//	SecStrPtr p;
//	char FromSSType, ToSSType;
//
///* set the Root to the current cartoon */
//	Root = Start;
//
///* Loop through symbols */
//	for (p=Start;p;p=p.To) 
//	{
//		if (p.SymbolPlaced)
//		{
//			switch (p.SecondaryStructureType) 
//			{
//				case 'E':	MakeObject( p.Direction=='U'?"UpTriangle":"DownTriangle", 
//							3, Verbatim( 1.0-p.Fill/100.0 ), PSxy(p.CartoonX), PSxy(p.CartoonY) );
//						break;
//				case 'H':	MakeObject( "Circle", 3, Verbatim( 1.0-p.Fill/100.0 ), PSxy(p.CartoonX), PSxy(p.CartoonY) );
//						break;
//			}
//		}
//	}
//
///* Loop Through lines */
//	for (p=Start;p;p=p.To)
//	{
//		if (p.SymbolPlaced)
//		{
//
//			ToSSType = p.SecondaryStructureType;
//
//			if (p.From) 
//			{
//
//				FromSSType = p.From.SecondaryStructureType;
//				if ( !( ((ToSSType=='C')||(ToSSType=='N')) && ((FromSSType=='C')||(FromSSType=='N')) ) )
//				{
//
//					if ( ncp = p.From.NConnectionPoints )
//					{
//
//						JoinPoints( PSxy(p.From.CartoonX), PSxy(p.From.CartoonY),
//								PSxy(p.From.ConnectionTo[0][0]),PSxy(p.From.ConnectionTo[0][1]),
//								p.From.Direction,'*',p.From.SecondaryStructureType,
//								p.SecondaryStructureType);
//
//						for ( i=0 ; i<(ncp-1) ; i++)
//						{
//							JoinPoints( PSxy(p.From.ConnectionTo[i][0]), PSxy(p.From.ConnectionTo[i][1]),
//									PSxy(p.From.ConnectionTo[i+1][0]), PSxy(p.From.ConnectionTo[i+1][1]),
//									'*','*',p.From.SecondaryStructureType,
//									p.SecondaryStructureType);
//						}
//
//						JoinPoints( PSxy(p.From.ConnectionTo[ncp-1][0]),
//								PSxy(p.From.ConnectionTo[ncp-1][1]),
//								PSxy(p.CartoonX),PSxy(p.CartoonY),'*',
//								p.Direction,p.From.SecondaryStructureType,
//								p.SecondaryStructureType);
//
//					}
//					else
//					{
//						JoinPoints( PSxy(p.From.CartoonX), PSxy(p.From.CartoonY), 
//							PSxy(p.CartoonX), PSxy(p.CartoonY), p.From.Direction,
//							p.Direction, p.From.SecondaryStructureType,
// 							p.SecondaryStructureType );
//					}
//
//				}
//
//				if (FromSSType == 'N') 
//				{
//					MakeObject( "Square", 2, PSxy(p.From.CartoonX), PSxy(p.From.CartoonY) );
//					PrintText( p.From.Label, PSxy(p.From.CartoonX), PSxy(p.From.CartoonY) );
//				}
//
//			}
//
//			if (ToSSType == 'C') 
//			{
//				MakeObject( "Square", 2, PSxy(p.CartoonX), PSxy(p.CartoonY) );
//				PrintText( p.Label, PSxy(p.CartoonX), PSxy(p.CartoonY) );
//			}
//
//		}
//
//	}
//
//	return;
//	
//}
//
//
///* 
//  a function which writes out postscript code to define the symbols used in cartoons
//*/
//void DefCartoonSymbols(){
//
//	DefineObject( "Square" );
//		Literal( "moveto" );
//		MoveRelative( PSxy(-Radius)/2.0, PSxy(Radius)/2.0 );
//		LineRelative( PSxy(Radius), 0.0 );
//		LineRelative( 0.0, PSxy(-Radius) );
//		LineRelative( PSxy(-Radius), 0.0 );
//		ClosePath();
//		Fill( 1.0 );
//	EndObject();
//
//	DefineObject( "Circle" );
//		sprintf( buffer, " %d %d %d arc",
//			ConvertXPoint( PSxy(Radius)*Scale ), 0, 360 );
//		Literal( buffer );
//		Fill( -1.0 );
//		OutLine();
//	EndObject();
//
//	DefineObject( "UpTriangle" );
//		Literal( "moveto" );
//		MoveRelative( 0.0, PSxy(Radius) );
//		LineRelative( PSxy(Radius)*sin(PI2/3.0), PSxy(Radius)*(cos(PI2/3.0)-1.0) );
//		LineRelative( -2.0*PSxy(Radius)*sin(PI2/3.0), 0.0 );
//		ClosePath();
//		Fill( -1.0 );
//		OutLine();
//	EndObject();
//
//	DefineObject( "DownTriangle" );
//		Literal ( "moveto" );
//		MoveRelative( 0.0, PSxy(-Radius) );
//		LineRelative( PSxy(Radius)*sin(PI2/3.0), PSxy(-Radius)*(cos(PI2/3.0)-1.0) );
//		LineRelative( -2.0*PSxy(Radius)*sin(PI/3.0), 0.0 );
//		ClosePath();
//		Fill( -1.0 );
//		OutLine();
//	EndObject();
//
//	DefineObject( "Line" );
//		Literal( "moveto" );
//		Literal( "lineto" );
//		OutLine();
//	EndObject();
//
//}
//
///* 
//	function cross_circle
//
//	Tom F. August 1992
//
//	Function to return crossing point at which a line from A, B 
//	crosses the circle X and Y are replaced by the crossing point
//*/
//
//void CrossCircle( float *X, float *Y, float A, float B )
//{
//	int	i;
//	float	itv=20.0, lx, ly, rx, ry;
//	float   nx, ny;
//
//	for ( i=0;i<=itv;i++,lx=rx,ly=ry) {
//		rx = *X + PSxy(Radius)*sin( (float) i*PI2/itv )*Scale;
//		ry = *Y + PSxy(Radius)*cos( (float) i*PI2/itv )*Scale;
//		if (i) {
//			if (LineCross(lx,ly,rx,ry,A,B,*X,*Y,&nx,&ny)) {
//				*X = nx;
//				*Y = ny;
//				break;
//			}
//		}
//	}
//}
//
///* 
//	function cross_up_triangle
//
//	Tom F. August 1992
//
//	Function to return crossing point at which a line from A, B 
//	crosses the triangle X and Y are replaced by the crossing point
//*/
//
//void CrossUpTriangle( float *X, float *Y, float A, float B )
//{
//	int	i;
//	float	itv=3.0, lx, ly, rx, ry;
//	float   nx, ny;
//
//	for ( i=0;i<=itv;i++,lx=rx,ly=ry) {
//		rx = *X + PSxy(Radius)*sin( (float) i*PI2/itv );
//		ry = *Y + PSxy(Radius)*cos( (float) i*PI2/itv );
//		if (i) {
//			if (LineCross(lx,ly,rx,ry,A,B,*X,*Y,&nx,&ny)) {
//				*X = nx;
//				*Y = ny;
//				break;
//			}
//		}
//	}
//}
//
///* 
//	function cross_down_triangle
//
//	Tom F. August 1992
//
//	Function to return crossing point at which a line from A, B 
//	crosses the triangle X and Y are replaced by the crossing point
//*/
//
//void CrossDownTriangle( float *X, float *Y, float A, float B )
//{
//	int	i;
//	float	itv=3.0, lx, ly, rx, ry;
//	float   nx, ny;
//
//	for ( i=0;i<=itv;i++,lx=rx,ly=ry) {
//		rx = *X + PSxy(Radius)*sin( (float) i*PI2/itv );
//		ry = *Y - PSxy(Radius)*cos( (float) i*PI2/itv );
//		if (i) {
//			if (LineCross(lx,ly,rx,ry,A,B,*X,*Y,&nx,&ny)) {
//				*X = nx;
//				*Y = ny;
//				break;
//			}
//		}
//	}
//}
//
///*
//	function join_points
//
//	Tom F. September 1992
//
//	Function to join two points
//*/
//
//void JoinPoints( float px, float py, float qx, float qy, char pd, char qd, char pt, char qt )
//{
//	if (pd != 'U' && pd != '*') {
//		if (pt == 'E')
//			CrossDownTriangle( &px, &py, qx, qy );
//		if (pt == 'H')
//			CrossCircle( &px, &py, qx, qy );
//	}
//	if (qd != 'D' && qd != '*') {
//		if (qt == 'E')
//			CrossUpTriangle( &qx, &qy, px, py );
//		if (qt == 'H')
//			CrossCircle( &qx, &qy, px, py );
//	}
//	MakeObject( "Line", 4, px, py, qx, qy );	
//}
//
///* 
//This file contains a set of routines to draw postscript files. Every
//task is translated to points
//
//Tom F. October 1992
//*/
//
//
///* 
//These folow variables are set for usage through out these functions
//and are only accessible from this file
//*/
//public static final double ITOC  =  2.540;       /* Inches to centimetres */
//public static final double CTOI  =  0.3937;      /* Centimetres to inches */
//public static final double PPI = 72;      /* Points per inch */
//public static final double A4_W  =  21.0;        /* A4 width */
//public static final double A4_H  =  29.7;        /* A4 height */
//FILE    *OUT;           /* Output file */
//double   WIDTH=A4_W;     /* A4 width */
//double   HEIGHT=A4_H;        /* A4 height */
//double   PWIDTH=A4_W;        /* Picture width */
//double   PHEIGHT=A4_H;       /* Picture height */
//double   XMIN=0.0;       /* X minimum */
//double   YMIN=0.0;       /* Y minimum */
//double   XMAX=A4_W;      /* X default scale = centimeters */
//double   YMAX=A4_H;      /* Y default scale = centimeters */
//double   XP=PPI/ITOC;        /* X default points per centimeter */
//double   YP=PPI/ITOC;        /* Y defulat points per centimeter */
//int OBJ=false;      /* Defining object? */
//String FONT=null;     /* Default font */
//int POINT=15;       /* Default point size */
//float   TEXTPOS=0.0;        /* Default text position */
//float   TEXTHGT=0.0;        /* Default text position */
//
///*
//    function point_to_cm
//
//    Tom F. November 1992
//
//    Function to convert pointsize to centremetres (useful with text)
//*/
//float PointToCm( float point )
//{
//    return point*ITOC/PPI;
//}
//
///*
//function open_postscript
//
//Tom F. October 1992
//
//This function opens a postscript file for output. The file requires
//the output device. If the file is "" ie. no name then output is
//sent to stdout. Function returns false on failure. This function
//writes all the header parts to the file
//*/
//int OpenPostscript( String output_file )
//{
//boolean test=true;
//OUT = stdout;
//if ( strlen( output_file ) ) {
//    if ((OUT=fopen( output_file, "w" ))==null) {
//        OUT = stdout;
//        test = false;
//    }
//} else {
//    OUT = stdout;
//}
//fprintf( OUT, "%%!PS-Adobe-1.0\n" );
//fprintf( OUT, "%%%%Creator: postgen written by Tom Flores\n" );
//fprintf( OUT, "%%%%Title: postscript procedure\n" );
//fprintf( OUT, "%%%%CreationDate: Unknown\n" );
//fprintf( OUT, "%%%%Pages: 1\n" );
//
//return test;
//}
//
///*
//function page_size
//
//Tom F. October 1992
//
//This function sets the page size
//*/
//void PageSize( float width, float height )
//{
//OUT = stdout;
//if (width > 0.0) WIDTH = width;
//if (height > 0.0) HEIGHT = height;
//if (WIDTH < PWIDTH) PWIDTH=WIDTH;
//if (HEIGHT < PHEIGHT) PHEIGHT=HEIGHT;
//fprintf( OUT, "%%%%BoundingBox: %.1f %.1f %.1f %.1f\n",
//    0.0, 0.0, WIDTH * PPI * CTOI,
//    HEIGHT * PPI * CTOI );
//}
//
///* 
//function picture_size
//
//Tom F. October 1992
//
//This function sets up the picture size
//*/  
//void PictureSize( float width, float height )
//{
//if (width > 0.0) PWIDTH=width;
//if (height > 0.0) PHEIGHT=height;
//}
//
///*
//function scale_page
//
//Tom F. October 1992
//
//This function sets the page scale
//*/
//void ScalePage( float xmin, float xmax, float ymin, float ymax )
//{
//OUT = stdout;
//if (xmin < xmax) {
//    XMIN = xmin;
//    XMAX = xmax;
//}
//if (ymin < ymax) {
//    YMIN = ymin;
//    YMAX = ymax;
//}
//XP = ( PWIDTH * PPI ) / ((XMAX-XMIN) * ITOC);
//YP = ( PHEIGHT * PPI ) / ((YMAX-YMIN) * ITOC);
//if (OUT && XMIN != 0.0 && YMIN != 0.0)
//    fprintf( OUT, "%.1f %.1f  translate\n", 
//         -XMIN*PWIDTH/(XMAX-XMIN)*PPI*CTOI,
//         -YMIN*PHEIGHT/(YMAX-YMIN)*PPI*CTOI );
//}
//
///* 
//function centre_page
//
//Tom F. October 1992
//
//This function centres a picture on a page
//*/
//void CentrePage( )
//{
//OUT = stdout;
//if (OUT)
//    fprintf( OUT, "%.1f %.1f translate\n", 
//         ((WIDTH-PWIDTH) / 2.0 * PPI * CTOI),
//         ((HEIGHT-PHEIGHT) /2.0 * PPI * CTOI) );
//}
//
///*
//function perimeter
//
//Tom F. October 1992
//
//This function draws a box round the picture
//*/
//void Perimeter( )
//{
//OUT = stdout;
//if (OUT) {
//    fprintf( OUT, "newpath\n" );
//    fprintf( OUT, "  %.1f %.1f moveto\n",  (XMIN*XP),  (YMIN*YP) );
//    fprintf( OUT, "  %.1f %.1f lineto\n",  (XMIN*XP),  (YMAX*YP) );
//    fprintf( OUT, "  %.1f %.1f lineto\n",  (XMAX*XP),  (YMAX*YP) );
//    fprintf( OUT, "  %.1f %.1f lineto\n",  (XMAX*XP),  (YMIN*YP) );
//    fprintf( OUT, "  %.1f %.1f lineto\n",  (XMIN*XP),  (YMIN*YP) );
//    fprintf( OUT, "  closepath\n" );
//    fprintf( OUT, "stroke\n" );
//}
//}
//
///*  
//function define_object
//
//Tom F. October 1992
//
//Function to begin an object definition
//*/
//void DefineObject( String object_name )
//{
//OUT = stdout;
//if (OUT)
//    fprintf( OUT, "/%s\n{ newpath\n", object_name );
//}
//
///* 
//function end_object
//
//Tom F. OCtober 1992
//
//Function to finish object definition
//*/
//void EndObject( )
//{
//OUT = stdout;
//if (OUT)
//    fprintf( OUT, " } def\n" );
//}
//
///*
//function make_object
//
//Tom F. October 1992
//
//This function uses a variable argument list, the first member 
//represents the object to be placed, the next is an integer that
//represents the number of of parameters to be stacked before the
//object is drawn these must all be floats!
//*/
//void MakeObject( String object_name, int i, double... args)
//{
//double  arg;
//OUT = stdout;
//
//if (OUT) {
//    fprintf( OUT, "  " );
//    va_start( ap, i );
//    for (double arg : args) {
//        fprintf( OUT, "%.1f ", (float) (arg*XP) );
//    }
//    va_end( ap );
//    fprintf( OUT, "%s\n", object_name );
//}
//}
//
///* 
//function translate
//D. Westhead 11/09/96
//*/
//void Translate( float x, float y)
//{
//OUT = stdout;
//if (OUT) fprintf( OUT, "  %.1f %.1f translate\n",  (x*XP),  (y*YP) );
//}
//
///*
//function move_to
//
//Tom F. October 1992
//*/
//void MoveTo( float x, float y )
//{
//OUT = stdout;
//if (OUT)
//    fprintf( OUT, "  %.1f %.1f moveto\n",  (x*XP),  (y*YP) );
//}
//
///*
//Function move_relative
//
//Tom F. October 1992
//*/
//void MoveRelative( float x, float y )
//{
//OUT = stdout;
//if (OUT)
//    fprintf( OUT, "  %.1f %.1f rmoveto\n",  (x*XP),  (y*YP) );
//}
//
///*
//function line_to
//
//Tom F. October 1992
//*/
//void LineTo( float x, float y )
//{
//OUT = stdout;
//if (OUT)
//    fprintf( OUT, "  %.1f %.1f lineto\n",  (x*XP),  (y*YP) );
//}
//
///*
//function line_relative
//
//Tom F. October 1992
//*/
//void LineRelative( float x, float y )
//{
//OUT = stdout;
//if (OUT)
//    fprintf( OUT, "  %.1f %.1f rlineto\n",  (x*XP),  (y*YP) );
//}
//
///*
//function close_path
//
//Tom F. October 1992
//*/
//void ClosePath( )
//{
//OUT = stdout;
//if (OUT)
//    fprintf( OUT, "  closepath\n" );
//}
//
///*
//function newpath
//
//Tom F. October 1992
//*/
//void NewPath( )
//{
//OUT = stdout;
//if (OUT)
//    fprintf( OUT, "newpath\n" );
//}
//
///*
//function end_postscript
//
//Tom F. October 1992
//
//Function to complete postscript picture
//*/
//void EndPostscript( )
//{
//OUT = stdout;
//if (OUT) {
//    fprintf( OUT, "showpage\n" );
//    if (OUT!=stdout) fclose( OUT );
//}
//}
//
///*
//function outline
//
//TOm F. October 1992
//*/
//void OutLine( )
//{
//OUT = stdout;
//if (OUT) 
//    fprintf( OUT, "  stroke\n" );
//}   
//
///*
//function fill
//
//Tom F. October 1992
//*/
//void Fill( double level )
//{
//OUT = stdout;
//if (OUT)
//    if (level >= 0.0 )
//        fprintf( OUT, "  gsave\n  %.2f setgray fill\n  grestore\n", level );
//    else
//        fprintf( OUT, "  gsave\n  setgray fill\n  grestore\n" );
//}
//
///*
//function line_width
//
//Tom F. October 1992
//*/
//void LineWidth( double width )
//{
//OUT = stdout;
//if (OUT)
//    fprintf( OUT, "  %.1f setlinewidth\n",  (width*XP) );
//}
//
///* 
//function literal
//
//Tom F. October 1992
//
//Function to allow literal translation of postscript ie. written to
//file directly - this allows for any additional bits to be added 
//freely
//*/
//void Literal( String postlit )
//{
//OUT = stdout;
//if (OUT)
//    fprintf( OUT, "  %s\n", postlit );
//}
//
///*
//function choose_font
//
//Tom F. October 1992
//
//function to select font
//*/
//void ChooseFont( String font, int point_size )
//{
//OUT = stdout;
//if (FONT) free(FONT);
//FONT = strdup( font );
//if (point_size > 0) POINT = point_size;
//if (OUT)
//    fprintf( OUT, "/%s findfont %d scalefont setfont\n",
//        FONT, POINT );
//}
//
///*
//function character_height
//
//Tom F. October 1992
//
//This function set the character height
//*/
//void CharacterHeight( float height )
//{
//OUT = stdout;
//if (POINT != height*YP && height*YP > 0.0) {
//    POINT = height*YP;
//    ChooseFont( FONT, POINT );
//}
//}
//
///*
//function print_text
//
//Tom F. October 1992
//
//Function to output text
//*/
//void PrintText( String text, float x, float y )
//{
//OUT = stdout;
//if (OUT && FONT) {
//    fprintf( OUT, "  %.1f %.1f moveto\n",  (x*XP), 
//         (y*YP - TEXTHGT*POINT) );
//    fprintf( OUT, "  (%s) dup stringwidth pop\n", text );
//    fprintf( OUT, "  %.2f mul 0 rmoveto\n", -TEXTPOS );
//    fprintf( OUT, "   show\n" );
//    
//}
//}
//
///*
//function text_centre
//
//Tom F. October 1992
//
//Function to determine text centre value as fraction
//*/
//void TextCentre( float length, float height )
//{
//TEXTPOS = length;
//TEXTHGT = height;
//}
//
///*
//function convert_x_point
//
//Tom F. October 1992
//
//This function converts a given x value to its point value
//*/
//int ConvertXPoint( double value )
//{
//return (int)(value*XP);
//}
//
//double Verbatim( double value )
//{
//return value/XP;
//}
//
///*
//function convert_y_point
//
//Tom F. October 1992
//
//This function converts a given y value to its point value
//*/
//int ConvertYPoint( double value )
//{
//return (int)(value*YP);
//}
//
//
///*
//function debug_ps
//
//Tom F. October 1992
//
//This function outputs the values and states of internal variables
//*/
//void debug_ps( )
//{
//fprintf( stderr, "PS - variables:\n" );
//fprintf( stderr, "WIDTH     = %f\n", WIDTH );
//fprintf( stderr, "HEIGHT    = %f\n", HEIGHT );
//fprintf( stderr, "PWIDTH    = %f\n", PWIDTH );
//fprintf( stderr, "PHEIGHT   = %f\n", PHEIGHT );
//fprintf( stderr, "XMIN      = %f\n", XMIN );
//fprintf( stderr, "XMAX      = %f\n", XMAX );
//fprintf( stderr, "YMIN      = %f\n", YMIN );
//fprintf( stderr, "YMAX      = %f\n", YMAX );
//fprintf( stderr, "XP        = %f\n", XP );
//fprintf( stderr, "YP        = %f\n", YP );
//}
//
//
//
//}