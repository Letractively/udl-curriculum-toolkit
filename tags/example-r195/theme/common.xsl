<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet[
<!ENTITY catts "@id|@class|@title|@xml:lang">
<!ENTITY cncatts "@id|@title|@xml:lang">
 ]>
<xsl:stylesheet
    version="1.0"
    xmlns:out="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:m="http://www.w3.org/1998/Math/MathML"
    xmlns:wicket="http://wicket.apache.org"
    xmlns="http://www.w3.org/1999/xhtml" 
    exclude-result-prefixes="dtb">
    
    <xsl:variable name="lower">abcdefghijklmnopqrstuvwxyz</xsl:variable>
    <xsl:variable name="upper">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
    
    <!-- Set up key on every annotation to the content it references - the first non annotation sibling -->
    <xsl:key name="annokey" match="dtb:annotation" use="following-sibling::*[local-name() != 'annotation'][1]/@id"/>

    <!-- prodnotes are pulled out as "d" links -->
    <xsl:template match="dtb:prodnote"/>

    <!-- pagenum naver displayed -->
    <xsl:template match="dtb:pagenum"/>
    
    <!-- For some reason span isn't handled in the standard dtbook2xhtml -->
    <xsl:template match="dtb:span">
      <span>
        <xsl:copy-of select="&catts;"/>
        <xsl:apply-templates/>
      </span>
    </xsl:template>

   	<xsl:template match="dtb:list[@type='ol']">
       <xsl:variable name="listtype">
           <xsl:choose>
               <xsl:when test="@enum='1'">decimal</xsl:when>
               <xsl:when test="@enum='i'">lower-roman</xsl:when>
               <xsl:when test="@enum='I'">upper-roman</xsl:when>
               <xsl:when test="@enum='a'">lower-alpha</xsl:when>
               <xsl:when test="@enum='A'">upper-alpha</xsl:when>
               <xsl:otherwise></xsl:otherwise>
           </xsl:choose>
       </xsl:variable>
       <ol>
           <xsl:copy-of select="&catts;"/>
           <xsl:if test="$listtype != ''">
               <xsl:attribute name="style">
                   <xsl:value-of select="concat('list-style-type: ', $listtype, ';')"/>
               </xsl:attribute>
           </xsl:if>
           <xsl:apply-templates/>
       </ol>
   	</xsl:template>
   	
   	<xsl:template match="dtb:table">
		<table class="dataTable {@class}">
			<xsl:copy-of select="&cncatts;"/>
			<xsl:apply-templates/>
		</table>
   </xsl:template>

    <xsl:template match="dtb:p">
        <p class="hlpassage {@class}">
            <xsl:copy-of select="&cncatts;"/>
            <xsl:apply-templates/>
        </p>
    </xsl:template>

	<xsl:template match="dtb:list/dtb:li">
		<li class="hlpassage">
			<xsl:copy-of select="&cncatts;"/>
			<xsl:apply-templates />
		</li>
	</xsl:template>

    <!-- Ignore except when called out by image. -->
    <xsl:template match="dtb:imggroup/dtb:caption"/>
    
    <xsl:template match="dtb:caption" mode="caption">
     <div class="imgCaption">
	       <xsl:copy-of select="&catts;"/>
	       <xsl:apply-templates/>
     </div>
   	</xsl:template>

    <xsl:template match="dtb:caption" mode="mediaCaption">
       <xsl:copy-of select="&catts;"/>
       <xsl:apply-templates/>
   	</xsl:template>


    <xsl:template name="basename">
        <xsl:param name="path"/>
        <xsl:choose>
            <xsl:when test="contains($path, '/')">
                <xsl:call-template name="basename">
                    <xsl:with-param name="path" select="substring-after($path, '/')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$path"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- TOGGLE BUTTONS -->
    <xsl:template match="dtb:div[@class='supplement']">
		<div class="supportBox collapseBox">
			<h4 wicket:id="collapseBoxControl-" class="toggleOffset">
				<xsl:value-of select="@title" />
			</h4>
			<div class="collapseBody">
				<xsl:apply-templates />
			</div>
		</div>
	</xsl:template>

    <!-- Slide Show -->
    <xsl:template match="dtb:div[@class='slideshow']">
		<div wicket:id="slideShow_" class="slideshow" id="{@id}">
			<div class="slideshowTitle">
				<xsl:value-of select="@title" />
			</div>
			<ul>
	         	<xsl:apply-templates select="dtb:div" mode="slideshowLink" />
			</ul>
			<xsl:apply-templates select="dtb:div" />
		</div>
	</xsl:template>

	<!-- these turn into buttons for the slide show  -->
    <xsl:template match="dtb:div" mode="slideshowLink">
    	<li>
    		<a href="#{@id}" title="{dtb:bridgehead/@title}"><xsl:value-of select="dtb:bridgehead" /></a>
    	</li>
   		<xsl:text> </xsl:text>
    </xsl:template>

	<!--  ignore all bridgheads inside the slideshow -->
    <xsl:template match="dtb:div[@class='slideshow']//dtb:bridgehead">
    </xsl:template>
    
    
    <!-- GLOSSARY - link used is determined by application parameter isi.glossary.type -->
    <xsl:template match="dtb:gl">
    	<!--  this is for inline glossary terms -->
  		<a wicket:id="glossword" onclick="return togglespan(this);" class="glossary" href="#">
        	<xsl:apply-templates/>
   		</a>
   		<xsl:text> </xsl:text>
   		<span class="inlineGlossary" style="display:none">
	   			<span wicket:id="glossdef" word="{@entryId}">definition will be inserted here</span>
  				<xsl:text> </xsl:text>
   				<a wicket:id="glosslink" word="{@entryId}">(more)</a>
  		</span>

    	<!--  this is for glossary links to modal popups -->
  		<a wicket:id="glossaryLink_" class="vocabulary" word="{@entryId}">
	    	<xsl:apply-templates/>
  		</a>
  		
    	<!--  this is for glossary links to main glossary page-->
  		<a wicket:id="glossaryMainLink_" class="vocabulary" word="{@entryId}">
	    	<xsl:apply-templates/>
  		</a>  		
	</xsl:template>


    <!-- VIDEOS, AUDIO -->
    <xsl:template match="dtb:object">
      <xsl:choose>
      
      <!-- if there is no source then there is an error -->
       <xsl:when test="@src=''">
        	<div style="border:5px inset red">Content error: Object with no src attribute set</div>
       </xsl:when>
       
       <xsl:when test="contains(@src, 'youtube.com') or contains(@src, 'youtu.be')">
			<xsl:variable name="width">
	           <xsl:choose>
	             <xsl:when test="@width"><xsl:value-of select="@width"/></xsl:when>
	             <xsl:otherwise>640</xsl:otherwise>
	           </xsl:choose>
	         </xsl:variable>
	        <xsl:variable name="height">
	           <xsl:choose>
	             <xsl:when test="@height"><xsl:value-of select="@height"/></xsl:when>
	             <xsl:otherwise>360</xsl:otherwise>
	           </xsl:choose>
	        </xsl:variable>
	        <xsl:variable name="src">
	          <xsl:choose>
	            <xsl:when test="contains(@src, '/watch?v=')">
	              <xsl:value-of select="concat('http://www.youtube-nocookie.com/embed/', substring-after(@src, 'watch?v='))"/>
				</xsl:when>
				<xsl:when test="contains(@src, 'youtu.be/')">
	              <xsl:value-of select="concat('http://www.youtube-nocookie.com/embed/', substring-after(@src, 'youtu.be/'))"/>
				</xsl:when>
				<xsl:otherwise><xsl:value-of select="@src"/></xsl:otherwise>
			  </xsl:choose>
			</xsl:variable>
			<div class="objectBox center">
				<div class="mediaPlaceholder" style="width:{$width}px; height:{$height}px;">
					<iframe width="{$width}" height="{$height}" src="{$src}" frameborder="0" class="captionSizer">must have content</iframe>
				</div>
				<xsl:call-template name="objectCaption" />
    		</div>
       </xsl:when>
      
 	   <xsl:when test="contains(@src, '.flv') or contains(@src, '.mp4') or contains(@src, '.mp3')">
         <!-- embedded movie -->
		 <xsl:call-template name="videotag" />
	   </xsl:when>
                   
 	   <xsl:otherwise>
 	     <!-- unknown object type -->
         <div wicket:id="object_" appletname="{@src}"  width="{@width}" height="{@height}" id="{@id}">
           <xsl:apply-templates/>
         </div>
		 <xsl:call-template name="objectCaption" />
       </xsl:otherwise>
   	 </xsl:choose>

    </xsl:template>

	<xsl:template name="objectCaption">
		<xsl:if test="count(child::dtb:caption) > 0 or count(child::dtb:prodnote) > 0">
			<div class="objectCaption">
		       	<div class="objectText">
		       		<xsl:if test="count(child::dtb:caption) > 0">
			       		<xsl:apply-templates select="child::dtb:caption[1]" mode="mediaCaption"/>
		       		</xsl:if>
					<!-- add the toggle if there is more than one caption or a prodnote - long description -->
					<xsl:if test="count(child::dtb:caption) > 1 or count(child::dtb:prodnote) > 0">
						<div class="collapseBox">
							<h5 wicket:id="objectToggleHeader_" src="{@src}">More Information</h5>
							<div class="collapseBody">
								<xsl:apply-templates
									select="child::dtb:caption[position()&gt;1]"
									mode="caption" />
								<xsl:apply-templates select="child::dtb:prodnote"
									mode="prodnote" />
							</div>
						</div>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
	</xsl:template>  
    
    
    <xsl:template match="dtb:param">
	  	<param>
	  		<xsl:copy-of select="@name|@value"/>
	  	</param>
    </xsl:template>
    

    <xsl:template name="videotag">
        <xsl:variable name="width">
            <xsl:choose>
                <xsl:when test="@width != ''">
                    <xsl:value-of select="@width" />
                </xsl:when>
                <xsl:otherwise>400</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="height">
            <xsl:choose>
				<xsl:when test="@height != ''">
                    <xsl:value-of select="@height" />
                </xsl:when>
                <!-- if there is no height for an mp3 just display the control bar -->
       			<xsl:when test="contains(@src, '.mp3')">
                    <xsl:text>25</xsl:text>
	            </xsl:when>
                <xsl:otherwise>
                    <xsl:text>170</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="poster" select="dtb:param[@name='poster']/@value"/>
		<xsl:variable name="captions" select="dtb:param[@name='captions']/@value"/>
		<xsl:variable name="audiodescription" select="dtb:param[@name='audiodescription']/@value"/>
        <div class="objectBox center">
			<div class="mediaPlaceholder" style="width:{$width}px; height:{$height}px;">
		        <div wicket:id="videoplayer_{@id}" width="{$width}" height="{$height}" src="{@src}">
		        	<xsl:if test="$poster">
		        		<xsl:attribute name="poster"><xsl:value-of select="$poster"/></xsl:attribute>
		        	</xsl:if>
		        	<xsl:if test="$captions">
		        		<xsl:attribute name="captions"><xsl:value-of select="$captions"/></xsl:attribute>
		        	</xsl:if>
		        	<xsl:if test="$audiodescription">
		        		<xsl:attribute name="audiodescription"><xsl:value-of select="$audiodescription"/></xsl:attribute>
		        	</xsl:if>
		        </div>
	        </div>
		 	<xsl:call-template name="objectCaption" />
        </div>
    </xsl:template>

	<!-- thumb ratings -->
	<xsl:template match="dtb:responsegroup[@class='thumbrating']">
 		<p>
     		<strong>Rate this:</strong>    
			<span wicket:id="thumbRating_" id="{@id}">
			</span>
		</p>
	</xsl:template>


    <!-- IMAGES -->
	<xsl:template match="dtb:imggroup">
      <xsl:choose>
  		<xsl:when test="count(./dtb:img) > 1 and @class!='smartImage'">
          <!-- found more than one image - group them -->
			<div class="imgGroup">
	          <xsl:apply-templates/>
			</div>
		</xsl:when>
        <xsl:when test="@class='right'">
          <!-- image float to the right side with text wrap -->
            <xsl:apply-templates/>
        </xsl:when>
        <xsl:when test="@class='left'">
          <!-- image on the left side with text wrap -->
            <xsl:apply-templates/>
        </xsl:when>
        <xsl:when test="@class='center'">
          <!-- image in the center with no text wrap -->
            <xsl:apply-templates/>
        </xsl:when>
        <xsl:when test="@class='smartImage'">
        	<div wicket:id="smartImage-{@id}" class="smartImage">
        		<xsl:copy-of select="&catts;" />
        		<xsl:call-template name="smartImageProcess" />
        	</div>
        </xsl:when>
        <xsl:when test="@class='annotatedImage'">
			<!-- the annotated image id is the id of the first img in this imggroup, send the src of the image for reference processing -->
	        <div wicket:id="annotatedImage_{@id}" annotatedImageId="{./dtb:img/@id[1]}" annotatedImageSrc="{./dtb:img/@src[1]}">
		    	<xsl:call-template name="annotatedImageProcess" />
	    	    <xsl:apply-templates select="key('annokey', @id)[@class='hotspot']" mode="hotspot"/>
	       	</div>
        </xsl:when>
        <xsl:otherwise>
	        <!-- featured image - floats left with no text wrap -->
	        <br clear="left"/>
	        <div class="imggroup">
		        <xsl:apply-templates/>
	        </div>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>
    
    <xsl:template name="smartImageProcess">
    	<xsl:copy-of select="dtb:img" />
    	<xsl:copy-of select="dtb:caption" />
    </xsl:template>

    <xsl:template name="annotatedImageProcess">
   		<xsl:apply-templates/>   		
    </xsl:template>

   	<xsl:template match="dtb:annotation" mode="hotspot">
		<!-- the annotated image id is the id of the img in the first sibling imggroup  -->
	     <span wicket:id="hotSpot_" style="display:none" annotatedImageId="{../dtb:imggroup/dtb:img/@id[1]}"
	     	title="{@title}" top="{@top}" left="{@left}" width="{@width}" height="{@height}"
	     	imgSrc="{@imgSrc}" imgClass="{@imgClass}">
		       <xsl:apply-templates/>
	     </span>
   	</xsl:template>

	<xsl:template name="modalImageDetail">
   		<xsl:variable name="addImageToggle">
         	<xsl:choose>
        		<xsl:when test="count(../dtb:caption) > 1 or ../dtb:prodnote">
	            		<xsl:value-of select="'true'" />
        		</xsl:when>
        		<xsl:otherwise>
	            		<xsl:value-of select="'false'" />
	            </xsl:otherwise>
	        </xsl:choose>
	    </xsl:variable>
   		<div class="modalBody" id="imageDetail_{@id}" style="display:none">
		    <div class="modalHeader" role="banner">
		        <div class="modalTitle"></div>
		        <a href="#" class="modalMove button icon"><img src="/img/icons/move.png" width="16" height="16" alt="Move" title="Move" /></a>
	      		<a href="#" class="modalClose button icon" onclick="showImageDetail('{@id}', false); return false">
	        		<img class="imageDetailButton" src="/img/icons/close.png"></img>
	        	</a>	
	    	</div>
		    <div class="modalMainCol">
		     	<div class="imgBox">
		     		<img wicket:id="image_{@id}" src="{@src}" class="captionSizer">
		       			<xsl:copy-of select="&cncatts;" />
		           		<xsl:copy-of select="@alt" />
		           		<xsl:apply-templates/>
		       		</img>
		       		<div class="imgCaption">
				       	<div class="imgText">
				       		<xsl:apply-templates select="../dtb:caption[@imgref=current()/@id][1]" mode="caption"/>
						    <!-- want this toggle when the image hasCaptions (more than one caption or a prodnote) -->
					       	<xsl:if test="$addImageToggle = 'true'">
			                    <div class="collapseBox">
			                        <h5 wicket:id="imgToggleHeader_">More Information</h5>
			                        <div class="collapseBody">
			                        <xsl:apply-templates select="../dtb:caption[@imgref=current()/@id][position()&gt;1]" mode="caption"/>
						       		<xsl:apply-templates select="../dtb:prodnote[@imgref=current()/@id]" mode="prodnote"/>
						       		</div>
						       	</div>
						    </xsl:if>
						</div>
		       		</div>
		        </div>
			</div>
		</div>
	</xsl:template>
	
	<xsl:template match="dtb:img">
		<!--  determine if the image detail toggle should be added, it must have either
			  more than one caption or a long description -->
   		<xsl:variable name="addImageToggle">
         	<xsl:choose>
        		<xsl:when test="count(../dtb:caption) > 1 or ../dtb:prodnote">
	            		<xsl:value-of select="'true'" />
        		</xsl:when>
        		<xsl:otherwise>
	            		<xsl:value-of select="'false'" />
	            </xsl:otherwise>
	        </xsl:choose>
	    </xsl:variable>
		<!--  determine if the larger image modal should be added, img must have class = thumb -->
   		<xsl:variable name="thumbImage">
         	<xsl:choose>
        		<xsl:when test="@class='thumb'">
	            		<xsl:value-of select="'true'" />
        		</xsl:when>
        		<xsl:otherwise>
	            		<xsl:value-of select="'false'" />
	            </xsl:otherwise>
	        </xsl:choose>
	    </xsl:variable>
		<div class="imgBox" id="image_{@id}">
	    	<xsl:choose>
    	    	<xsl:when test="ancestor::dtb:imggroup and ancestor::dtb:imggroup[@class='right']">
        	    	<xsl:attribute name="class">imgBox right</xsl:attribute>
         		</xsl:when>
    	    	<xsl:when test="ancestor::dtb:imggroup and ancestor::dtb:imggroup[@class='left']">
        	    	<xsl:attribute name="class">imgBox left</xsl:attribute>
         		</xsl:when>
    	    	<xsl:when test="ancestor::dtb:imggroup and ancestor::dtb:imggroup[@class='center']">
        	    	<xsl:attribute name="class">imgBox center</xsl:attribute>
         		</xsl:when>
        	</xsl:choose>
		  	<xsl:if test="$thumbImage = 'false'">
	        	<img wicket:id="image_{@id}" src="{@src}" class="captionSizer">
		       		<xsl:copy-of select="&cncatts;" />
		        	<xsl:copy-of select="@alt" />
		        	<xsl:copy-of select="@height" />
		        	<xsl:copy-of select="@width" />
		        	<xsl:apply-templates/>
		       	</img>
	       	</xsl:if>
		  	<xsl:if test="$thumbImage = 'true'">
	        	<img wicket:id="imageThumb_{@id}" src="{@src}" class="thumb captionSizer">
	            	<xsl:copy-of select="&cncatts;" />
	            	<xsl:copy-of select="@alt" />
	        	</img>
        	</xsl:if>
        	<!-- only add the caption div if there is a caption, prodnote, or thumbnail -->
        	<xsl:if test="count(../dtb:caption) > 0 or count(../dtb:prodnote) > 0 or $thumbImage = 'true'">
		        <div class="imgCaption">
				  	<xsl:if test="$thumbImage = 'true'">
			        	  <div class="imgActions">
				            <span wicket:id="imageDetailButton_{@id}" target="{@src}"></span>
				         </div>
				    </xsl:if>
			       	<div class="imgText">
			       		<xsl:apply-templates select="../dtb:caption[@imgref=current()/@id][1]" mode="caption"/>
					    <!-- want this toggle when the image hasCaptions (more than one caption or a prodnote) -->
				       	<xsl:if test="$addImageToggle = 'true'">
		                    <div class="collapseBox">
				                <h5 wicket:id="imgToggleHeader_">More Information</h5>
		                        <div class="collapseBody">
		                        <xsl:apply-templates select="../dtb:caption[@imgref=current()/@id][position()&gt;1]" mode="caption"/>
					       		<xsl:apply-templates select="../dtb:prodnote[@imgref=current()/@id]" mode="prodnote"/>
					       		</div>
					       	</div>
					    </xsl:if>
					</div>
				</div>
			</xsl:if>
	    </div>
	    <!-- add the hidden modal with the larger image -->
	  	<xsl:if test="$thumbImage = 'true'">
	  		<xsl:call-template name="modalImageDetail"/>
	  	</xsl:if>	
	</xsl:template>
    
	<!--  prodnotes inside of img groups are long descriptions -->
    <xsl:template match="dtb:prodnote" mode="prodnote">
     <div class="longDescription">
       <xsl:copy-of select="&catts;"/>
       <xsl:apply-templates/>
     </div>
   </xsl:template>

  <!-- response groups -->
   <xsl:template match="dtb:responsegroup">
   	 <xsl:variable name="type">
		<xsl:choose>
			<xsl:when test="dtb:select1">select1</xsl:when>
			<xsl:otherwise>responsearea</xsl:otherwise>
		</xsl:choose>
	 </xsl:variable>
     <div class="entryBox nohlpassage">
     	<div class="teacherBar" wicket:id="teacherBar_">
     		<div class="teacherBarLeft">
     	    </div>
      	    <div class="teacherBarRight">
      	    	<!-- ADD the teacher specific annotation WHAT TO LOOK FOR here -->
        		<a wicket:id="compareResponses_" href="#" class="button" rgid="{ancestor-or-self::dtb:responsegroup/@id}" type="{$type}">Compare Responses</a>
            	<span wicket:id="feedbackButton_" for="teacher" rgid="{ancestor-or-self::dtb:responsegroup/@id}"></span>
       	 	</div>
       </div>
       <xsl:apply-templates select="dtb:prompt"/>
       <xsl:call-template name="responseArea"/>
     </div>
   </xsl:template>

	<xsl:template name="responseArea">
		<xsl:choose>
			<xsl:when test="dtb:select1">
				<form wicket:id="select1_"
					rgid="{ancestor-or-self::dtb:responsegroup/@id}"
					title="{ancestor-or-self::dtb:responsegroup/@title}"
					group="{ancestor-or-self::dtb:responsegroup/@group}"
					class="subactivity">					
					<div class="responseBar">
						<div class="responseLeft"><!-- empty --></div>
						<div class="responseRight">
							<!-- helper links -->
							<xsl:apply-templates select="key('annokey', @id)" mode="showannotations" />
							<span wicket:id="feedbackButton_" for="student" rgid="{ancestor-or-self::dtb:responsegroup/@id}"></span>
							<span wicket:id="mcScore"></span>
						</div>
					</div>
					<xsl:apply-templates select="dtb:select1" />
				</form>
			</xsl:when>
			<xsl:otherwise>
				<div class="responseBar">
					<div wicket:id="responseButtons_" rgid="{@id}" class="responseLeft">
					</div>
					<div class="responseRight">
						<!-- helper links -->
						<xsl:apply-templates select="key('annokey', @id)" mode="showannotations" />
						<span wicket:id="feedbackButton_" for="student" rgid="{ancestor-or-self::dtb:responsegroup/@id}"></span>
					</div>
				</div>
				<!-- list of responses -->
				<div wicket:id="responseList_" rgid="{@id}" group="{@group}">
				</div>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Multiple Choice -->
	<xsl:template match="dtb:select1">
		<div class="responseItem" wicket:id="radioGroup">
        	<div class="responseMCItem">
				<xsl:apply-templates select="dtb:item//dtb:label" />
			</div>
			<p class="responseMCActions">
				<a href="#" wicket:id="submitLink" class="button">Check My Answer</a>	
			</p>
			<div class="responseMCFeedback">
				<xsl:apply-templates select="dtb:item//dtb:message" />
				<wicket:enclosure child="selectNone">
					<div class="stResult incorrect">
						<p wicket:id="selectNone" >Select an answer...</p>
					</div>
				</wicket:enclosure>
			</div>
		</div>
	</xsl:template>

	<xsl:template match="dtb:item//dtb:label">
		<xsl:variable name="itemid" select="concat('selectItem_', ancestor::dtb:responsegroup/@id, '_', ancestor::dtb:item/@id)"/>
		<xsl:variable name="labelid" select="concat('selectItemLabel_', ancestor::dtb:responsegroup/@id, '_', ancestor::dtb:item/@id)"/>
		<div wicket:id="{$itemid}" class="responseMCItem">
			<xsl:copy-of select="ancestor::dtb:item/@correct" />
			<input wicket:id="radio" type="radio">
   				<xsl:copy-of select="&catts;" />
			</input>
			<label wicket:id="label">
				<xsl:apply-templates />
			</label>
		</div>
	</xsl:template>

    <xsl:template match="dtb:item//dtb:message">
		<xsl:variable name="itemid" select="concat('selectItem_', ancestor::dtb:responsegroup/@id, '_', ancestor::dtb:item/@id)"/>
		<xsl:variable name="messageid" select="concat('selectMessage_', ancestor::dtb:responsegroup/@id, '_', ancestor::dtb:item/@id)"/>
		<div wicket:id="{$messageid}" for="{$itemid}">
			<xsl:attribute name="class">
				<xsl:choose>
					<xsl:when test="ancestor::dtb:item/@correct='true'">stResult correct</xsl:when>
					<xsl:otherwise>stResult incorrect</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:apply-templates />
		</div>
    </xsl:template>

   <!-- ratings  -->
	<xsl:template match="dtb:responsegroup[@class='rating']">
		<div wicket:id="ratePanel_" id="{@id}" type="{@type}">
            <xsl:apply-templates select="dtb:prompt" />
		</div>
	</xsl:template>
      
   <xsl:template match="dtb:prompt">
     <div class="prompt" id="prompt_{ancestor::dtb:responsegroup/@id}">
       <xsl:apply-templates/>
     </div>
   </xsl:template>
   
   <!-- MathML  -->
    <xsl:template match="m:math">
      <!-- The non-standard "ignore" attribute tells TextHelp toolbar not to mark up inside the span.
           The class "nohlpassage" tells the highlighting code not to add its spans.
           Any added spans mess up the math display. -->
      <span ignore="true" class="nohlpassage">
        <math>
          <xsl:copy-of select="@*"/>
          <xsl:apply-templates/>
        </math>
      </span>
    </xsl:template>
    
    <xsl:template match="m:*">
      <!-- MathJax does not want the elements to be namespaced. -->
      <xsl:element name="{local-name()}">
        <xsl:copy-of select="@*"/>
      	<xsl:apply-templates/>
      </xsl:element>
    </xsl:template>
    
</xsl:stylesheet>