#include<stdlib.h>
#include<stdio.h>
#include<math.h>

struct TEMPS
{
	int H, M, S;	
};

 struct ATHLET 
 {
 	char nom;
 	char prenom;
 	int numero;
 	struct TEMPS resultat; 
 };
 
int TRANSFORM(struct TEMPS T){
	return T.S+T.M*60+T.H*3600;	
}

struct TEMPS DECOMPOSE (  int S)
{   
	struct TEMPS T;

	T.H= S/3600;  S=S%3600;
	T.M=S/60;     T.S=S%60;
	return T;
	;
}

main(){
	struct TEMPS T;
	printf("hours\n");
	scanf("%d", &T.H);
	printf("minutes\n");
	scanf("%d", &T.M);
	printf("seconds\n");
	scanf("%d", &T.S);
	int secondes_transform=TRANSFORM(T);
	
	printf("donner des secondes a decomposer");
	int secondes_a_decomposer=0;
	scanf("%d",&secondes_a_decomposer);
	struct TEMPS T2=DECOMPOSE(secondes_a_decomposer);
	printf( "Transform a coverti en %d secondes \n \n Decompose a decomposer les secondes en %d secondes %d minutes %d heures",secondes_transform,T2.S,T2.M,T2.H);

    struct ATHLET athlet[100]{
    int nbrAthlt; int i ;
    printf("donner le nombre d’athletes:\n");
	scanf("%d",&nbrAthlt);
	
	for(i=0;i<nbrAthlt;i++){
	 	printf("\n \n \n");
		printf("Entrer athlete N %d:\n", (i+1));
		
		scanf("%s",athlet[i].nom);
		scanf("%s",athlet[i].prenom);
		scanf("%d",&athlet[i].numero); 
		scanf("%d",&athlet[i].resultat.H);
		scanf("%d",&athlet[i].resultat.M);
		scanf("%d",&athlet[i].resultat.S);
	}
	
	for(i=0;i<nbrAthlt;i++){
		printf("l'athlete n %d : \n nom: %s \n prenom:%s \n numero:%d \n resultat: %d %d %d ",
			(i+1), athlet[i].nom, athlet[i].prenom, athlet[i].numero, athlet[i].resultat.H, athlet[i].resultat.M, athlet[i].resultat.S);
	}
    };

}
