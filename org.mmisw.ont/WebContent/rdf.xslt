<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" exclude-result-prefixes="rdf">





    <xsl:template match="/" >
        
        <table cellspacing="6">
                <xsl:apply-templates/>

        </table>

     

    </xsl:template>

    <xsl:template match="rdf:RDF">
        <xsl:apply-templates select="rdf:Description"/>
    </xsl:template>

    <xsl:template match="rdf:Description">

       
            <tr bgcolor="#99CCFF">
                <td colspan="2">
                    <xsl:element name="h4">
                        <xsl:value-of select="@rdf:about"/>
                        <xsl:text xml:space="preserve">   </xsl:text>
                    </xsl:element>
                </td>
            </tr>

            <xsl:for-each select="child::node()">
               
                
                <xsl:if test="string-length(local-name(.))>2 and not(@rdf:nodeID)">
                    <xsl:element name="tr">

                        <xsl:attribute name="bgcolor">#99FFFF</xsl:attribute>
                        <td>
                          
                            <xsl:copy-of select="local-name(.)"/>
                            <xsl:text>: </xsl:text>

                        </td>
                        
                        <td>
                            
                            <xsl:choose>
                                <xsl:when test="not(@rdf:resource)">
                                    <xsl:value-of select="current()"/>
                                    
                                </xsl:when>
                                <xsl:when test="@rdf:resource">
                                    <xsl:value-of select="@rdf:resource"/>
                                </xsl:when>
                            </xsl:choose>
                         
                           
                        </td>

                    </xsl:element>
             </xsl:if>
            </xsl:for-each>
     
    </xsl:template>


</xsl:stylesheet>