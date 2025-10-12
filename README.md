# psiw-p

TODO:
1. Endpoint dla movie details razem ze zdjęciem (przechowywanie zdjec)
2. Refresh token endpoint
3. Uzywac clock.now() do czasu
4. ticket_owner camelCase
5. Domyslny status - TO_BE_CALCULATED
   Wtedy liczony jest wlasciwy status na podstawie czasu
   wiecej niz 15 minut przed -> INVALID
   mniej niz 15 przed -> VALID
   po seansie -> expired 
   przy skanowaniu zmienia sie na USED

6. map Duration to minutes in output dtos
7. roomNumber -> String
8. exceptions (literowka)
9. poprzenosic z utils ???
    